package org.springframework.rest.documentation.doclet;

/**
 * @author WangBei
 * @since 2015/3/21
 */
public class DocletTest {
    public static void main(String[] args) {
        com.sun.tools.javadoc.Main.execute(new String[]{
              "-doclet", RestDoclet.class.getName(),
              "-sourcepath", "rest-documentation-example/src/main/java",
              "-d", "rest-documentation-example/target/classes",
              "warbaby.test"
        });
    }
}
