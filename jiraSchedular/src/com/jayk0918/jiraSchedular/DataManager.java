package com.jayk0918.jiraSchedular;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class DataManager {
	
	// 데이터 저장
    public void saveData(List<Task> taskList, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(taskList);
            oos.flush();
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving data to file.");
        }
    }

    // 데이터 불러오기
    @SuppressWarnings("unchecked")
	public List<Task> loadData(String filename) {
        List<Task> taskList = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            taskList = (List<Task>) ois.readObject();
            System.out.println("Data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return taskList;
    }
}
