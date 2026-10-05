module com.mycompany.fanmanagerproject {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;

    opens com.mycompany.fanmanagerproject to javafx.graphics;
    exports com.mycompany.fanmanagerproject;
}