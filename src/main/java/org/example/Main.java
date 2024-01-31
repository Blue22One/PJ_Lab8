package org.example;

import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/lab8";
        String sql = "select * from Persoane";
        Connection connection = DriverManager.getConnection(url, "root", "admin");
        Statement statement;
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = statement.executeQuery(sql);
        afisare_tabela(rs, "Continut initial tabela Persoane");
        System.out.println();
        int opt, nr;
        Scanner scanner = new Scanner(System.in);
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println("0. Iesire");
            System.out.println("1. Adaugare persoana");
            System.out.println("2. Adaugare excursie");
            System.out.println("3. Afisare persoane si excursii in care au fost");
            System.out.println("4. Afișarea excursiilor în care a fost o persoană al cărei nume se citește de la tastatură");
            System.out.println("5. Afișarea tuturor persoanelor care au vizitat o anumita destinație");
            System.out.println("6. Afișarea persoanelor care au făcut excursii într-un an introdus");
            System.out.println("7. Ștergerea unei excursii");
            System.out.println("8. Ștergerea unei persoane (împreună cu excursiile în care a fost)");
            System.out.print("Introduceti optiunea dvs: ");
            opt = scan.nextInt();
            switch (opt) {
                case 0:
                    System.exit(0);
                    break;
                case 1:
                    nr = getRowCount(connection, "persoane") + 1;
                    System.out.println("Introduceti numele:");
                    String numeKey = scanner.next();
                    System.out.println("Introduceti varsta:");
                    int varstaKey = scanner.nextInt();

                    adaugare(rs, nr, numeKey, varstaKey);
                    afisare_tabela(rs, "Continut Persoane dupa adaugare:");
                    break;
                case 2:
                    int id_exc = getRowCount(connection, "excursii") + 1;

                    System.out.println("Introduceti datele excursiei:");
                    System.out.println("Id_persoana:");
                    int id_pers = scanner.nextInt();
                    System.out.println("Anul:");
                    int an = scanner.nextInt();
                    System.out.println("Destinatia:");
                    String dest = scanner.next();

                    String sql2 = "select * from Excursii";
                    ResultSet rs2 = statement.executeQuery(sql2);

                    if (checkPers(connection, id_pers))
                        adaugare2(rs2, id_pers, id_exc, dest, an);
                    else
                        System.out.println("Eroare! ID persoana inexistent.");
                    afisare_tabela2(rs2, "Continut Excursii dupa adaugare");
                    break;
                case 3:
                    afisare_tot(connection, "Persoanele si excursiile in care au fost:");
                    break;
                case 4:
                    String n = scanner.next();
                    afisare4(connection, n);
                    break;
                case 5:
                    String d = scanner.next();
                    afisare5(connection, d);
                    break;
                case 6:
                    int a = scanner.nextInt();
                    afisare6(connection, a);
                    break;
                case 7:
                    System.out.println("Dati id-ul excursiei de sters:");
                    int sters = scanner.nextInt();
                    stergere7(connection, sters);

                    String sql_ = "select * from Excursii";
                    ResultSet rs_ = statement.executeQuery(sql_);
                    afisare_tabela2(rs_, "Continut Excursii dupa stergere");
                    break;
                case 8:
                    String nK = scanner.next();
                    stergere8(connection, nK);
                    break;
                default:
                    System.out.println("Optiune indisponibila! Va rugam reincercati!");
                    break;
            }
        } while (opt != 0);

        scan.close();
        scanner.close();
        connection.close();
        statement.close();
        rs.close();
    }

    public static void afisare_tabela(ResultSet rs, String mesaj) {
        System.out.println("\n---" + mesaj + "---");
        try {
            rs.beforeFirst();
            while (rs.next())
                System.out.println("id=" + rs.getInt(1) + ", nume=" + rs.getString(2) + ",varsta="
                        + rs.getInt(3));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void afisare_tabela2(ResultSet rs, String mesaj) {
        System.out.println("\n---" + mesaj + "---");
        try {
            rs.beforeFirst();
            while (rs.next())
                System.out.println("id_persoane=" + rs.getInt(1) + ", id_excursie=" + rs.getInt(2) + ",destinatia="
                        + rs.getString(3) + ", anul=" + rs.getInt(4));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void adaugare(ResultSet rs, int id, String nume, int varsta) {
        try {
            rs.moveToInsertRow();
            rs.updateInt("id", id);
            rs.updateString("nume", nume);
            rs.updateInt("varsta", varsta);
            rs.insertRow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void adaugare2(ResultSet rs, int id_pers, int id_exc, String dest, int an) {
        try {
            rs.moveToInsertRow();
            rs.updateInt("id_persoane", id_pers);
            rs.updateInt("id_excursie", id_exc);
            rs.updateString("destinatia", dest);
            rs.updateInt("anul", an);
            rs.insertRow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getRowCount(Connection connection, String tableName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                // Retrieve the count from the result set
                return resultSet.getInt(1);
            }
        }
        // Return -1 if an error occurs
        return -1;
    }

    private static boolean checkPers(Connection connection, int id_pers) throws SQLException {
        String query = "SELECT * FROM persoane WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id_pers);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void afisare_tot(Connection connection, String mesaj) throws SQLException {
        System.out.println("\n---" + mesaj + "---");
        String query = "SELECT A.nume, B.destinatia, B.anul "
                + "FROM persoane A, excursii B " + "WHERE A.id = B.id_persoane";

        try (PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                System.out.println("Nume_pers: " + rs.getString("nume") + ", destinatie: " + rs.getString("destinatia")
                        + ", an: " + rs.getInt("anul"));
        }
    }

    public static void afisare4(Connection connection, String numeKey) {
        String query = "SELECT persoane.nume, excursii.destinatia, excursii.anul FROM excursii, persoane " +
                "WHERE persoane.nume = ? AND persoane.id = excursii.id_persoane";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, numeKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    System.out.println("Destinatie: " + rs.getString("destinatia") + ", an: " + rs.getInt("anul"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void afisare5(Connection connection, String destKey) throws SQLException {
        String query = "SELECT persoane.nume FROM persoane, excursii WHERE persoane.id = excursii.id_persoane AND excursii.destinatia = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, destKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    System.out.println("Nume: " + rs.getString("nume"));
            }
        }
    }

    public static void afisare6(Connection connection, int anKey) throws SQLException {
        String query = "SELECT persoane.nume FROM persoane, excursii WHERE persoane.id = excursii.id_persoane AND excursii.anul = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, anKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    System.out.println("Nume: " + rs.getString("nume"));
            }
        }
    }

    public static void stergere7(Connection connection, int id_sterg) {
        String query = "DELETE FROM excursii WHERE id_excursie = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id_sterg);
            int nr_rand = ps.executeUpdate();
            System.out.println("Excursia a fost stearsa! Nr randuri: " + nr_rand);
        } catch (SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }

    public static void stergere8(Connection connection, String numeKey) {
        String query = "DELETE FROM excursii WHERE id_persoane = (SELECT id from persoane WHERE nume = ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, numeKey);
            int nr_rand = ps.executeUpdate();
            System.out.println("Persoana si inregistrarile aferente au fost sterse! Nr randuri afectate: " + nr_rand);
        } catch (SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }

        query = "DELETE FROM persoane WHERE nume = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, numeKey);
            int nr_rand = ps.executeUpdate();
            System.out.println("Persoana si inregistrarile aferente au fost sterse! Nr randuri afectate: " + nr_rand);
        } catch (SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }
}