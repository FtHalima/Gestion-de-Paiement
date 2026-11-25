package com.gestionpaiements.app.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Choix des en-têtes PDF, conservé indépendamment des données de paiement. */
public final class PdfVersion {
    public enum Version {
        ANCIEN("Ancien", "images/Logo1.png"),
        NOUVEAU("Nouveau", "images/logo-nouveau.png");

        private final String libelle;
        private final String logo;

        Version(String libelle, String logo) {
            this.libelle = libelle;
            this.logo = logo;
        }

        public String getLibelle() { return libelle; }
        public String getLogo() { return logo; }
    }

    private static final Path FICHIER = Path.of(System.getProperty("user.home"),
            "GestionPaiements", "pdf-version.properties");
    private static volatile Version courante = charger();

    private PdfVersion() {}

    public static Version getCourante() { return courante; }

    private static Version charger() {
        if (!Files.exists(FICHIER)) return Version.ANCIEN;
        Properties properties = new Properties();
        try (var input = Files.newInputStream(FICHIER)) {
            properties.load(input);
            return Version.valueOf(properties.getProperty("version", "ANCIEN"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Préférence PDF illisible : utilisation du logo ancien.");
            return Version.ANCIEN;
        }
    }

    public static synchronized void choisir(Version version) throws IOException {
        java.util.Objects.requireNonNull(version);
        Files.createDirectories(FICHIER.getParent());
        Path temporaire = Files.createTempFile(FICHIER.getParent(), "pdf-version-", ".tmp");
        try {
            Properties properties = new Properties();
            properties.setProperty("version", version.name());
            try (var output = Files.newOutputStream(temporaire)) {
                properties.store(output, "Version des en-tetes PDF");
            }
            try {
                Files.move(temporaire, FICHIER, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaire, FICHIER, StandardCopyOption.REPLACE_EXISTING);
            }
            courante = version;
        } finally {
            Files.deleteIfExists(temporaire);
        }
    }
}
