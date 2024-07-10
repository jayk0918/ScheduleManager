package com.jayk0918.jiraSchedular;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class ScheduleManager extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JTable table;
    private DefaultTableModel tableModel;
    private DataManager dataManager;
    private final String dataFileName = "task_data.dat";
    
    public ScheduleManager() {
        setTitle("JIRA MINI 일정 관리 프로그램");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        dataManager = new DataManager();
        
        // 데이터 불러오기
        List<Task> initialData = dataManager.loadData(dataFileName);
        if (initialData != null) {
        	tableModel = new DefaultTableModel(new Object[]{"CR번호", "제목", "반영일", "진행 상황", "비고"}, 0);
            for (Task task : initialData) {
                tableModel.addRow(new Object[]{
                        task.getCrNumber(), task.getTitle(), task.getReflectionDate(),
                        task.getProgress(), task.getRemarks()
                });
            }
        }
        
        // 테이블 모델 초기화
        if (tableModel == null) {
            tableModel = new DefaultTableModel(new Object[]{"CR번호", "제목", "반영일", "진행 상황", "비고"}, 0);
            table.setModel(tableModel); // 테이블에 모델 설정
        }
        
        table = new JTable(tableModel){
        	private static final long serialVersionUID = 1L;
        	@Override
        	public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
        		Component c = super.prepareRenderer(renderer, row, column);
        		Color deployed = new Color(255, 153, 255);
        		if(!isRowSelected(row)){
        			String status = (String) getModel().getValueAt(row, 3);
    				if( "preqa uploaded".equals(status) || "confirm required".equals(status)){
						c.setBackground(Color.CYAN);
					} else if("in progress".equals(status)){
						c.setBackground(Color.GREEN.brighter());
			        } else if("holding".equals(status)){
			        	c.setBackground(Color.YELLOW.brighter());
			        } else if("confirmed".equals(status) || "deployed to prod".equals(status)){
			        	c.setBackground(deployed);
			        } else{
			        	c.setBackground(Color.WHITE);
			        }
        		}
        		return c;
    		}
        };
        
        // 각 열의 셀 렌더러 설정
        DefaultTableCellRenderer centerRenderer = new CenterRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowTransferHandler(table));

        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JTable table = (JTable) e.getSource();
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                table.getSelectionModel().setSelectionInterval(row, row);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        add(panel, BorderLayout.SOUTH);

        JButton addButton = new JButton("추가");
        panel.add(addButton);

        JButton removeButton = new JButton("삭제");
        panel.add(removeButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.addRow(new Object[]{"CRHQ-", "", "", "open", ""});
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int confirm = JOptionPane.showConfirmDialog(null, "정말로 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        tableModel.removeRow(selectedRow);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "삭제할 행을 선택하세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"open", "in progress", "holding", "preqa uploaded", "confirm required", "confirmed", "deployed to prod"});
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusComboBox));
        
        // 프로그램 종료 시 데이터 저장
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
					saveDataToFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }
    
    private void saveDataToFile() throws IOException {
        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String crNumber = (String) tableModel.getValueAt(i, 0);
            String title = (String) tableModel.getValueAt(i, 1);
            String reflectionDate = (String) tableModel.getValueAt(i, 2);
            String progress = (String) tableModel.getValueAt(i, 3);
            String remarks = (String) tableModel.getValueAt(i, 4);
            Task task = new Task(crNumber, title, reflectionDate, progress, remarks);
            taskList.add(task);
        }
        dataManager.saveData(taskList, dataFileName);
    }
    
    private class TableRowTransferHandler extends TransferHandler {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JTable table;

        public TableRowTransferHandler(JTable table) {
            this.table = table;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            int[] selectedRows = table.getSelectedRows();
            List<Object[]> rows = new java.util.ArrayList<>();
            for (int row : selectedRows) {
                int columnCount = table.getColumnCount();
                Object[] rowData = new Object[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    rowData[col] = table.getValueAt(row, col);
                }
                rows.add(rowData);
            }
            return new RowsTransferable(rows);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(RowsTransferable.ROWS_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            JTable.DropLocation dropLocation = (JTable.DropLocation) support.getDropLocation();
            int dropRow = dropLocation.getRow();
            Transferable transferable = support.getTransferable();

            try {
                @SuppressWarnings("unchecked")
				List<Object[]> rows = (List<Object[]>) transferable.getTransferData(RowsTransferable.ROWS_FLAVOR);

                DefaultTableModel model = (DefaultTableModel) table.getModel();
                for (Object[] rowData : rows) {
                    model.insertRow(dropRow, rowData);
                    dropRow++;
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE) {
                JTable table = (JTable) source;
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int[] selectedRows = table.getSelectedRows();
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    model.removeRow(selectedRows[i]);
                }
            }
        }
    }
    
    private static class RowsTransferable implements Transferable {
        public static final DataFlavor ROWS_FLAVOR = new DataFlavor(List.class, "application/x-java-rows");
        private final List<Object[]> rows;

        public RowsTransferable(List<Object[]> rows) {
            this.rows = rows;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{ROWS_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ROWS_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return rows;
        }
    }
    
    private static class CenterRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public CenterRenderer() {
            setHorizontalAlignment(CENTER); // 셀 가운데 정렬 설정
        }
    }
    

    public static void main(String[] args) {
    	try {
            // task_data.dat 파일 존재 여부 확인
            File dataFile = new File("task_data.dat");
            if (!dataFile.exists()) {
                System.out.println("task_data.dat 파일이 존재하지 않습니다. 새로 생성합니다.");
                dataFile.createNewFile();
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new ScheduleManager().setVisible(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            // 예외 처리 로직 추가 (파일 생성 실패 시)
            JOptionPane.showMessageDialog(null, "task_data.dat 파일을 생성할 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
}