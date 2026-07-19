package at.ee.pkms.pkmsbackend.capture;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pkms")
public class NoteCaptureProperties {

    private final Vault vault = new Vault();
    private final Sync sync = new Sync();

    public Vault getVault() {
        return vault;
    }

    public Sync getSync() {
        return sync;
    }

    public static class Vault {
        private Path inboxPath = Path.of("./vault/Inbox");
        private String attachmentDirectory = "_attachments";

        public Path getInboxPath() {
            return inboxPath;
        }

        public void setInboxPath(Path inboxPath) {
            this.inboxPath = inboxPath;
        }

        public String getAttachmentDirectory() {
            return attachmentDirectory;
        }

        public void setAttachmentDirectory(String attachmentDirectory) {
            this.attachmentDirectory = attachmentDirectory;
        }
    }

    public static class Sync {
        private Path indexPath = Path.of("./data/synced-captures.properties");

        public Path getIndexPath() {
            return indexPath;
        }

        public void setIndexPath(Path indexPath) {
            this.indexPath = indexPath;
        }
    }
}
