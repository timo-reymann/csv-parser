package com.github.timo_reymann.csv_parser.demo;

import com.github.timo_reymann.csv_parser.io.CsvWriter;

import java.io.File;
import java.io.IOException;

/**
 * @author Timrey
 * @since 20.12.17
 */
public class Program {
    public static void main(String... args) throws IllegalAccessException, IOException, InstantiationException {
     /*   CsvReader<MyBean> reader = new CsvReader.Builder<MyBean>()
                .forClass(MyBean.class)
                .file(new File("test.csv"))
                .hasHeading()
                .build();

        reader.lines()
                .forEach(System.out::println);
*/
     CsvWriter<MyBean> writer = new CsvWriter.Builder<MyBean>()
             .forClass(MyBean.class)
             .file(new File("test.csv"))
             .noAppend()
             .build();
        MyBean myBean = new MyBean();
        myBean.setEmail("test@gmx.de");
        myBean.setFirstName("Test");
        myBean.setLastName("Last");
        myBean.setGender("male");
        myBean.setId(1);
        myBean.setIp("wer");
        writer.writeLine(myBean);
        writer.close();
    /*    reader.lines()
                .forEach(bean -> {
                    try {
                        writer.writeLine(bean);
                    } catch (IllegalAccessException | IOException e) {
                        e.printStackTrace();
                    }
                });
        writer.close();*/
    }
}
