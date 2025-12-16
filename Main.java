package point_of_sale_system;

import javax.swing.*;                       // Import Swing components (GUI)
import javax.swing.border.EmptyBorder;      // Import spacing borders
import javax.swing.table.*;                 // Import Table models
import java.awt.*;                          // Import Graphics & Colors
import java.awt.print.*;                    // Import Printing logic
import javax.print.*;                       // Import Print Service lookup
import java.util.ArrayList;                 // Import dynamic arrays

public class Main {

    // --- 1. CONFIGURATION & THEME ---
    static final Color DARK_BG = Color.decode("#1F2B3E");   // deep blue background
    static final Color PANEL_BG = Color.decode("#2D3E55");  // Lighter blue for panels
    static final Color ACCENT = Color.decode("#00ADB5");    // Cyan accent color
    static final Color GREEN = Color.decode("#00C851");     // Success/Total color
    static final Color RED = Color.decode("#FF4444");       // Error/Delete color
    static final Color WHITE = Color.WHITE;                 // Standard white text
    static final Font F_MAIN = new Font("Segoe UI", Font.PLAIN, 14); // Standard font
    static final Font F_BOLD = new Font("Segoe UI", Font.BOLD, 14);  // Bold font

    static JFrame frame;                                    // Main application window
    static JPanel mainCardPanel;                            // Panel holder for screens
    static CardLayout cardLayout = new CardLayout();        // Layout to switch screens
    static double dailySales = 0;                           // Tracks total sales revenue

    public static void main(String[] args) {
        // --- WINDOW SETUP ---
        frame = new JFrame("Bandoy POS System");            // Create window with title
        frame.setSize(850, 600);                            // Set window dimensions
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Stop app on close
        frame.setLocationRelativeTo(null);                  // Center window on screen
        frame.setIconImage(new ImageIcon("logo.jpg").getImage()); // Set app icon

        // --- SCREEN MANAGER ---
        mainCardPanel = new JPanel(cardLayout);             // Init panel with CardLayout
        mainCardPanel.add(createLoginScreen(), "LOGIN");    // Add Login screen to deck
        
        frame.add(mainCardPanel);                           // Add deck to frame
        frame.setVisible(true);                             // Show the window
    }

    // --- 2. LOGIN SCREEN UI ---
    static JPanel createLoginScreen() {
        JPanel p = new JPanel(null);                        // layout set to null (absolute)
        p.setBackground(DARK_BG);                           // Apply theme background

        JLabel logo = new JLabel(resizeIcon("logo.jpg", 100, 100)); // Load & resize logo
        logo.setBounds(375, 50, 100, 100);                  // Position logo
        
        JLabel title = createLabel("SYSTEM LOGIN", 325, 160, 200, 30, F_BOLD, WHITE); // Main Title
        title.setHorizontalAlignment(SwingConstants.CENTER); // Center text

        JLabel lblRole = createLabel("Select Role", 275, 210, 300, 20, F_MAIN, Color.LIGHT_GRAY);
        JComboBox<String> boxRole = new JComboBox<>(new String[]{"Select", "admin", "cashier"}); // Dropdown
        styleComponent(boxRole, 275, 235, 300, 35);         // Apply visual styles

        JLabel lblPass = createLabel("Password", 275, 280, 300, 20, F_MAIN, Color.LIGHT_GRAY);
        JPasswordField txtPass = new JPasswordField();      // Masked text field
        styleComponent(txtPass, 275, 305, 300, 35);         // Apply visual styles

        JButton btnLogin = createButton("LOGIN", 275, 370, 300, 40, ACCENT); // Login button

        btnLogin.addActionListener(_ -> {                   // Button click event
            String role = (String) boxRole.getSelectedItem(); // Get selected role
            String pass = new String(txtPass.getPassword());  // Get typed password

            if ((role.equals("admin") && pass.equals("admin123")) || // Check Admin creds
                (role.equals("cashier") && pass.equals("1234"))) {   // Check Cashier creds
                
                mainCardPanel.add(createPOSScreen(role.toUpperCase()), "POS"); // Create POS UI
                cardLayout.show(mainCardPanel, "POS");      // Switch to POS screen
                txtPass.setText(""); boxRole.setSelectedIndex(0); // Reset fields
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid Credentials"); // Show error
            }
        });

        p.add(logo); p.add(title); p.add(lblRole); p.add(boxRole); p.add(lblPass); p.add(txtPass); p.add(btnLogin);
        return p;                                           // Return the panel
    }

    // --- 3. POS SCREEN UI ---
    static JPanel createPOSScreen(String role) {
        JPanel p = new JPanel(null);                        // Absolute layout panel
        p.setBackground(DARK_BG);                           // Theme background
        Cart cart = new Cart();                             // Logic for holding items

        // --- HEADER ---
        JLabel logo = new JLabel(resizeIcon("logo.jpg", 50, 50)); // Small logo
        logo.setBounds(30, 20, 50, 50);
        p.add(logo);

        p.add(createLabel("BANDOY POS", 90, 20, 200, 30, new Font("Segoe UI", Font.BOLD, 22), WHITE)); 
        p.add(createLabel("User: " + role, 92, 50, 200, 20, F_MAIN, ACCENT)); // Show active user

        JButton btnLogout = createButton("LOGOUT", 700, 25, 100, 30, RED); // Logout button
        btnLogout.addActionListener(_ -> cardLayout.show(mainCardPanel, "LOGIN")); // Go back to login
        p.add(btnLogout);

        // --- TABLE SETUP ---
        DefaultTableModel model = new DefaultTableModel(new String[]{"Item Name", "Price (Php)", "Qty", "Total (Php)"}, 0); 
        JTable table = new JTable(model);                   // Create table with model
        styleTable(table);                                  // Apply colors/fonts to table
        JScrollPane scroll = new JScrollPane(table);        // Add scrollbar
        scroll.setBounds(30, 190, 770, 280);                // Position table
        scroll.getViewport().setBackground(DARK_BG);        // Dark scroll area
        scroll.setBorder(BorderFactory.createLineBorder(PANEL_BG)); // Subtle border
        p.add(scroll);

        // --- FOOTER LABEL ---
        JLabel lblTotal = createLabel("TOTAL: Php 0.00", 30, 490, 400, 40, new Font("Segoe UI", Font.BOLD, 28), GREEN);
        p.add(lblTotal);

        // --- ADMIN ONLY FEATURE (Daily Sales) ---
        if(role.equals("ADMIN")) {                          // If user is Admin
            JButton btnRep = createButton("DAILY SALES", 540, 25, 150, 30, PANEL_BG); 
            btnRep.setForeground(ACCENT);                   // Cyan text
            btnRep.addActionListener(_ -> JOptionPane.showMessageDialog(frame, "Total Sales: Php " + String.format("%.2f", dailySales)));
            p.add(btnRep);                                  // Show accumulated sales
        }

        // --- DELETE BUTTON ---
        JButton btnDelete = createButton("DELETE ITEM", 620, 80, 180, 35, RED);
        btnDelete.addActionListener(_ -> {
            int selectedRow = table.getSelectedRow();       // Get index of clicked row
            
            if(selectedRow != -1) {                         // If a row is actually selected
                cart.list.remove(selectedRow);              // Remove from data list
                model.removeRow(selectedRow);               // Remove from visual table
                lblTotal.setText("TOTAL: Php " + String.format("%.2f", cart.total())); // Update total
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an item to delete."); // Error msg
            }
        });
        p.add(btnDelete);

        // --- INPUTS ---
        p.add(createLabel("Item Name", 30, 100, 200, 20, F_MAIN, Color.LIGHT_GRAY));
        JTextField txtItem = new JTextField(); styleComponent(txtItem, 30, 125, 250, 35);
        p.add(txtItem);

        p.add(createLabel("Price (Php)", 300, 100, 200, 20, F_MAIN, Color.LIGHT_GRAY));
        JTextField txtPrice = new JTextField(); styleComponent(txtPrice, 300, 125, 180, 35);
        p.add(txtPrice);

        p.add(createLabel("Qty", 500, 100, 200, 20, F_MAIN, Color.LIGHT_GRAY));
        JTextField txtQty = new JTextField(); styleComponent(txtQty, 500, 125, 100, 35);
        p.add(txtQty);

        JButton btnAdd = createButton("ADD", 620, 125, 180, 35, ACCENT); // Add item button
        p.add(btnAdd);

        JButton btnPrint = createButton("CHECKOUT & PDF", 550, 490, 250, 45, GREEN); // Print button
        p.add(btnPrint);

        // --- LOGIC: ADD BUTTON ---
        btnAdd.addActionListener(_ -> {
            try {
                String n = txtItem.getText();                       // Get name
                double pr = Double.parseDouble(txtPrice.getText()); // Get price
                int q = Integer.parseInt(txtQty.getText());         // Get quantity
                
                Item i = new Item(n, pr, q);                // Create item obj
                cart.add(i);                                // Add to cart logic
                model.addRow(new Object[]{i.n, i.p, i.q, i.sub()}); // Add to GUI table
                lblTotal.setText("TOTAL: Php " + String.format("%.2f", cart.total())); // Update total label
                
                txtItem.setText(""); txtPrice.setText(""); txtQty.setText(""); // Clear inputs
            } catch(Exception ex) { JOptionPane.showMessageDialog(frame, "Invalid Input"); } 
        });

        // --- LOGIC: CHECKOUT BUTTON ---
        btnPrint.addActionListener(_ -> {
            if(cart.list.isEmpty()) return;                 // Prevent empty checkout
            dailySales += cart.total();                     // Add to global admin sales
            
            PrinterJob job = PrinterJob.getPrinterJob();    // Init print job
            job.setPrintable(new Receipt(cart));            // Set the visual to print
            try {
                PrintService[] services = PrinterJob.lookupPrintServices(); // Find printers
                for(PrintService s : services) {
                    // Try to auto-select "Print to PDF"
                    if(s.getName().toLowerCase().contains("pdf") || s.getName().toLowerCase().contains("microsoft")) { 
                        job.setPrintService(s);             
                        break; 
                    }
                }
                job.print();                                // Trigger print dialog/action
                JOptionPane.showMessageDialog(frame, "PDF Saved Successfully!");
                
                cart.list.clear(); model.setRowCount(0); lblTotal.setText("TOTAL: Php 0.00"); // Reset POS

            } catch(Exception ex) { JOptionPane.showMessageDialog(frame, "Printing Error"); }
        });

        return p;
    }

    // --- 4. HELPER METHODS ---
    static JLabel createLabel(String t, int x, int y, int w, int h, Font f, Color c) {
        JLabel l = new JLabel(t); l.setBounds(x, y, w, h); l.setFont(f); l.setForeground(c);
        return l; // Factory method to create styled labels
    }

    static JButton createButton(String t, int x, int y, int w, int h, Color bg) {
        JButton b = new JButton(t); b.setBounds(x, y, w, h); b.setBackground(bg); 
        b.setForeground(WHITE); b.setFont(F_BOLD); b.setFocusPainted(false); b.setBorderPainted(false);
        return b; // Factory method to create styled buttons
    }

    static void styleComponent(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h); c.setBackground(PANEL_BG); c.setForeground(WHITE); c.setFont(F_MAIN);
        if(c instanceof JTextField) {
            ((JTextField)c).setCaretColor(WHITE);           // Set blinking cursor color
            ((JTextField)c).setBorder(new EmptyBorder(0, 10, 0, 10)); // Inner padding
        }
    }

    static ImageIcon resizeIcon(String path, int w, int h) {
        // Reads image -> Scales it -> returns Icon
        return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    static void styleTable(JTable t) {                      
        t.setBackground(PANEL_BG); t.setForeground(WHITE); t.setRowHeight(30); t.setFont(F_MAIN);
        t.setGridColor(DARK_BG); t.setShowVerticalLines(false); // Remove vertical grid
        
        JTableHeader h = t.getTableHeader();                // Get header object
        h.setBackground(DARK_BG); h.setForeground(WHITE); h.setFont(F_BOLD);
        h.setPreferredSize(new Dimension(0, 40));           // Header height
        
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);       // Center alignment object
        t.setDefaultRenderer(Object.class, center);         // Apply centering to cells
    }
}

// --- 5. DATA CLASSES ---

class Item { 
    String n; double p; int q;                              // Name, Price, Qty
    Item(String n, double p, int q){this.n=n;this.p=p;this.q=q;} 
    double sub(){return p*q;}                               // Calculate Subtotal
}

class Cart { 
    ArrayList<Item> list = new ArrayList<>();               // List storage
    void add(Item i){list.add(i);}                          // Add item to list
    double total(){return list.stream().mapToDouble(Item::sub).sum();} // Sum all subtotals
}

class Receipt implements Printable {
    Cart c; Receipt(Cart c){this.c=c;}                      // Constructor accepts cart
    
    public int print(Graphics g, PageFormat pf, int pi) {
        if(pi>0) return NO_SUCH_PAGE;                       // Only page 0 exists
        Graphics2D g2 = (Graphics2D)g; 
        g2.translate(pf.getImageableX(), pf.getImageableY()); // Adjust margins
        
        int y=20, x=10;                                     // Cursor coordinates
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));  // Typewriter font
        g2.drawString("BANDOY POS RECEIPT", x+20, y+=20);   // Draw Title
        
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12)); 
        g2.drawString("-------------------------------------", x, y+=20);
        g2.drawString(String.format("%-15s %5s %10s", "ITEM", "QTY", "PHP"), x, y+=20); // Columns
        g2.drawString("-------------------------------------", x, y+=20);
        
        for(Item i : c.list) {                              // Loop through cart items
            String n = i.n.length() > 12 ? i.n.substring(0,12)+"." : i.n; // Truncate name
            g2.drawString(String.format("%-15s %5d %10.2f", n, i.q, i.sub()), x, y+=20); // Draw row
        }
        
        g2.drawString("-------------------------------------", x, y+=20);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));  
        g2.drawString("TOTAL: Php " + String.format("%.2f", c.total()), x, y+=30); // Draw Total
        
        return PAGE_EXISTS;                                 // Tell printer page is ready
    } 
}