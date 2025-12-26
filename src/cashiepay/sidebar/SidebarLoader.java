package cashiepay.sidebar;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class SidebarLoader {

    public static AnchorPane getSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(
                SidebarLoader.class.getResource("/cashiepay/sidebar/SBfxml.fxml")
            );

            AnchorPane sidebar = loader.load();

            SBfxmlController controller = loader.getController();
            controller.initController(); // role applied HERE

            return sidebar;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
