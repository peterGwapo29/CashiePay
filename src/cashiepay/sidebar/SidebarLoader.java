package cashiepay.sidebar;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class SidebarLoader {

    private static AnchorPane loadedSidebar;

    public static AnchorPane getSidebar() {
        if (loadedSidebar == null) {
            try {
                FXMLLoader loader = new FXMLLoader(SidebarLoader.class.getResource("/cashiepay/sidebar/SBfxml.fxml"));
                loadedSidebar = loader.load();

                SBfxmlController controller = loader.getController();
                controller.initController();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return loadedSidebar;
    }
}
