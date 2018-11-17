# CSV-Parser
Parse csv files and other seperated values using java.

## Limitations
Currently all primitive types are suppported, plus LocalDate and LocalDateTime.

To use primitive types you must use the boxed types.

# How to use?

## Add to your depenencies

```xml
<dependency>
    <groupId>com.github.timo-reymann</groupId>
    <artifactId>csv-parser</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Create your bean class

Please keep in mind that you need an zero-args constructor for this parser to work properly!

### ... using the index for mapping
```java
@Data
public class MyBean {
    @CsvColumn(index = 0)
    private Integer id;

    @CsvColumn(index = 1)
    private String firstName;

    @CsvColumn(index = 2)
    private String lastName;

    @CsvColumn(index = 3)
    private String email;

    @CsvColumn(index = 4)
    private String gender;

    @CsvColumn(index = 5)
    private String ip;
}
```

### ... using the heading for mapping
```java
@Data
class MyBean {
    @CsvColumn(headerName="id")
    private Integer id;

    @CsvColumn(headerName="first_name")
    private String firstName;

    @CsvColumn(headerName="last_name")
    private String lastName;

    @CsvColumn(headerName="email")
    private String email;

    @CsvColumn(headerName="gender")
    private String gender;

    @CsvColumn(headerName="ip_address")
    private String ip;
}
```

## Write csv file

````java
CsvWriter<MyBean> writer = new CsvWriter.Builder<MyBean>()
             .forClass(MyBean.class)            // entity class
             .file(new File("customers.csv"))   // file
             .noAppend()                        // replace file every time
             .build();

// Create bean and set values
MyBean myBean = new MyBean();
myBean.setId(1);
myBean.setFirstName("Foo");
myBean.setLastName("Bar");
myBean.setEmail("foo@bar.com");
myBean.setGender("christmasTree");
myBean.setIp("127.0.0.1");

// Map object and add to file buffer
writer.writeLine(myBean);

// Write changes to disk
writer.close();
````

## Read csv file

```java
CsvReader<MyBean> reader = new CsvReader.Builder<MyBean>()
                .forClass(MyBean.class)         // bean class object
                .file(new File("test.csv"))     // specify file
                .inputStream(myInputStream)        // or even stream
                .hasHeading()                   // file has headings
                .build();

// Read all lines and print to console
reader.lines().forEach(System.out::println);
```


## Java 9/10/11
The parser is compatible with Java 9+! There are only two things for you to do:
1. Add to your module: ``requires com.github.timo_reymann.csv_parser``
2. Open your package for reflection containing bean classes like this: ``opens my.entities to com.github.timo_reymann.csv_parser``

Thats it!

## JavaDoc
If you are looking for the JavaDoc, its [here](https://www.javadoc.io/doc/com.github.timo-reymann/csv-parser/)

## Need further information?
Just send me a mail :)


## Found a bug?
[Create a issue](https://github.com/timo-reymann/csv-parser/issues/new)

