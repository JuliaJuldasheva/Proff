package ru.q1w2e3.chat.client;

import com.sun.xml.internal.bind.api.impl.NameConverter;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HistoryManager {

    private String login;
    private File file;
    private StringBuilder history;


    public HistoryManager(String login) {
        this.login = login;
        file = new File("client/src/main/resources/history/history_" + login + ".txt");
        if(!file.exists()) writeHistory(" ");
    }

    public void writeHistory(String msg) {
        try(OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
            out.write(msg + "\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String readLastLines() {
        history=new StringBuilder();
        List<String> result = new ArrayList<>();
        try(ReversedLinesFileReader reader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
            String line;
            int linesCount = 0;
            while((line = reader.readLine()) !=null && linesCount < 100) {
                result.add(line);
                linesCount++;
            }
            Collections.reverse(result);

            for (String o: result) {
                history.append(o).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history.toString();
    }
}
