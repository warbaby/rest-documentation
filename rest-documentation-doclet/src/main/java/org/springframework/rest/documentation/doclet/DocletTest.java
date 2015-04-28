package org.springframework.rest.documentation.doclet;

/**
 * @author WangBei
 * @since 2015/3/21
 */
public class DocletTest {
    public static void main(String[] args) {
        com.sun.tools.javadoc.Main.execute(new String[]{
              "-doclet", RestDoclet.class.getName(),
              //"-sourcepath", "rest-documentation-example/src/main/java",
              "-sourcepath", "D:\\code\\YD-svn\\cyyd-1.0-java\\cyou-cyuc-core\\src\\main\\java",
              "-subpackages", "com.changyou",
              "-d", "rest-documentation-example/target/classes",
              "warbaby.test"
        });
    }
}
