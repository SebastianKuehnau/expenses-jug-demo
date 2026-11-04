import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
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
        var all = new ArrayList<>(viennaReceipts());
        all.addAll(dublinReceipts());
        return all;
    }

    /** German receipts from Austria. */
    static List<Receipt> viennaReceipts() {
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

    /** English receipts from Dublin (the euro is Ireland's currency, so the amounts fit the app). */
    static List<Receipt> dublinReceipts() {
        return List.of(
            new Receipt("6-train-ticket-dublin-galway.png", "TRAIN TICKET", List.of(
                "Emerald Rail Ireland", "Ticket No. 4410 2873 06", "",
                "Dublin Heuston  ->  Galway Ceannt", "Standard class, return leg",
                "Date: 14 Oct 2026", "",
                "TOTAL             42.50 EUR", "incl. 13.5% VAT", "", "Paid by credit card")),
            new Receipt("7-hotel-invoice-dublin.png", "HOTEL INVOICE", List.of(
                "The Liffey Bridge Hotel", "14 Eden Quay, Dublin 1, D01 X2Y4",
                "Invoice No. LB-2026-30518", "",
                "Guest: M. Kuehn", "Check-in 14 Oct 2026, check-out 15 Oct 2026", "",
                "1 night single room       149.00 EUR", "Breakfast                   19.00 EUR", "",
                "TOTAL (incl. 13.5% VAT)   168.00 EUR", "", "Payment: credit card")),
            new Receipt("8-pub-dinner-with-pints.png", "RECEIPT", List.of(
                "The Liffey Arms", "22 Crown Alley, Dublin 2, D02 K7P3", "Check 8841   Table 12", "",
                "Date: 14 Oct 2026  20:15", "",
                "2x Irish stew           38.00", "2x Starter              16.00",
                "2x Apple tart           14.00", "2x Pint of stout        12.40", "",
                "SUBTOTAL                80.40 EUR", "Service charge (12.5%)  10.05 EUR",
                "TOTAL                   90.45 EUR", "", "Thank you for dining with us!")),
            new Receipt("9-cloud-hosting-invoice.png", "INVOICE", List.of(
                "Celtic Cloud Ltd", "12 Grand Canal Dock, Dublin 4, D04 R5T2", "Invoice No. CC-440217", "",
                "Date: 1 Oct 2026", "",
                "Cloud Hosting Pro, annual plan", "1 account   1 Oct 2026 - 30 Sep 2027", "",
                "Net            405.69 EUR", "23% VAT         93.31 EUR", "",
                "TOTAL          499.00 EUR", "", "Payment by direct debit")),
            new Receipt("10-taxi-dublin.png", "TAXI RECEIPT", List.of(
                "Dublin Radio Cabs", "Trip No. 90412", "", "Date: 16 Oct 2026   07:40",
                "From: Heuston Station", "To: Dublin Airport", "Distance: 14.2 km", "",
                "Fare           34.50 EUR", "incl. 13.5% VAT", "",
                "TOTAL          34.50 EUR", "", "Paid by card")));
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
