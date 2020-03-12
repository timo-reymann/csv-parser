module com.github.timo_reymann.csv_parser {
    requires static lombok;
    requires transitive org.mapstruct.processor;
    exports com.github.timo_reymann.csv_parser.io;
    exports com.github.timo_reymann.csv_parser.meta;
    exports com.github.timo_reymann.csv_parser.exception;
}