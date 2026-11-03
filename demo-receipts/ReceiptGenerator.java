import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Draws the demo receipts (all invented: made-up companies, no real logos).
 * Run from the project root: {@code java demo-receipts/ReceiptGenerator.java}
 */
public class ReceiptGenerator {

    record Receipt(String file, String title, List<String> lines) {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        for (Receipt r : receipts()) {
            draw(r);
            System.out.println("demo-receipts/" + r.file());
        }
    }

    static List<Receipt> receipts() {
        return List.of(
            new Receipt("1-bahnticket-wien-graz.png", "BAHNTICKET", List.of(
                "Bahnreisen Österreich", "Fahrkarte Nr. 7731 4420 19", "",
                "Wien Hbf  ->  Graz Hbf", "Railjet, 2. Klasse", "Datum: 14.10.2026", "",
                "Fahrpreis        65,50 EUR", "20% USt          13,10 EUR", "",
                "GESAMT           78,60 EUR", "", "Bezahlt mit Kreditkarte")),
            new Receipt("2-hotelrechnung-graz.png", "HOTELRECHNUNG", List.of(
                "Hotel Murblick Graz", "Lendkai 12, 8020 Graz", "Rechnung Nr. 2026-10471", "",
                "Gast: M. Kühn", "Anreise 14.10.2026, Abreise 15.10.2026", "",
                "1 Nacht Einzelzimmer      139,00 EUR", "Frühstück                  18,00 EUR",
                "Ortstaxe                    3,50 EUR", "",
                "GESAMT (inkl. 10% USt)   160,50 EUR", "", "Zahlung: Kreditkarte")),
            new Receipt("3-restaurant-mit-wein.png", "RESTAURANT", List.of(
                "Gasthaus Zur Traube", "Hauptplatz 3, 8010 Graz", "Beleg 5527   Tisch 9", "",
                "Datum: 14.10.2026  20:41", "",
                "2x Hauptspeise Wild     64,00", "2x Vorspeise            19,80",
                "2x Dessert              14,60", "2x Flasche Blaufränkisch 2021   58,00", "",
                "SUMME                  156,40 EUR", "Trinkgeld               13,60 EUR",
                "GESAMT                 170,00 EUR", "", "Vielen Dank für Ihren Besuch!")),
            new Receipt("4-software-abo.png", "RECHNUNG", List.of(
                "Codeforge GmbH", "Softwarestraße 1, 1010 Wien", "Rechnung Nr. CF-88213", "",
                "Datum: 01.10.2026", "",
                "CodeForge Pro, Jahresabo", "1 Lizenz   01.10.2026 - 30.09.2027", "",
                "Netto           99,17 EUR", "20% USt         19,83 EUR", "",
                "GESAMT         119,00 EUR", "", "Zahlung per SEPA-Lastschrift")),
            new Receipt("5-taxi-wien.png", "TAXI-QUITTUNG", List.of(
                "Funktaxi Wien", "Fahrt Nr. 330914", "", "Datum: 16.10.2026   07:52",
                "Von: Wien Hbf", "Nach: Flughafen Wien", "Distanz: 17,4 km", "",
                "Fahrpreis      39,80 EUR", "inkl. 13% USt", "",
                "GESAMT         39,80 EUR", "", "Bar bezahlt")));
    }

    static void draw(Receipt r) throws Exception {
        int width = 640;
        int height = 140 + r.lines().size() * 34 + 80;
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(236, 233, 226));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.fillRect(40, 20, width - 80, height - 40);
        g.setColor(new Color(30, 30, 30));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        g.drawString(r.title(), 70, 80);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 22));
        int y = 130;
        for (String line : r.lines()) {
            g.drawString(line, 70, y);
            y += 34;
        }
        g.dispose();
        ImageIO.write(image, "png", new File("demo-receipts", r.file()));
    }
}
