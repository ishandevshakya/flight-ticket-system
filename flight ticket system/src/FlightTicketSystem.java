import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FlightTicketSystem {

    private JFrame frame;

    public FlightTicketSystem() {
        frame = new JFrame("Flight Ticket System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new GridLayout(6, 1));

        JButton bookTicketButton = new JButton("Book Ticket");
        bookTicketButton.addActionListener(e -> bookTicket());

        JButton searchTicketButton = new JButton("Search Ticket");
        searchTicketButton.addActionListener(e -> searchTicket());

        JButton searchFlightsButton = new JButton("Search Flights");
        searchFlightsButton.addActionListener(e -> searchFlights());

        JButton cancelTicketButton = new JButton("Cancel Ticket");
        cancelTicketButton.addActionListener(e -> cancelTicket());

        JButton addFlightButton = new JButton("Add Flight");
        addFlightButton.addActionListener(e -> addFlight());

        JButton deleteFlightButton = new JButton("Delete Flight");
        deleteFlightButton.addActionListener(e -> deleteFlight());

        frame.add(bookTicketButton);
        frame.add(searchTicketButton);
        frame.add(searchFlightsButton);
        frame.add(cancelTicketButton);
        frame.add(addFlightButton);
        frame.add(deleteFlightButton);

        frame.setVisible(true);
    }

    private void bookTicket() {
        String name = JOptionPane.showInputDialog("Enter your name:");
        String flightId = JOptionPane.showInputDialog("Enter flight ID:");
        String response = sendPostRequest("http://localhost:3000/book-ticket",
                "{\"name\":\"" + name + "\",\"flightId\":\"" + flightId + "\"}");
        showResponse(response);
    }

    private void searchTicket() {
        String ticketId = JOptionPane.showInputDialog("Enter ticket ID:");
        String response = sendGetRequest("http://localhost:3000/search-ticket/" + ticketId);
        showResponse(response);
    }

    private void searchFlights() {
        String departure = JOptionPane.showInputDialog("Enter departure city:");
        String arrival = JOptionPane.showInputDialog("Enter arrival city:");
        String response = sendGetRequest(
                "http://localhost:3000/search-flights?departure=" + departure + "&arrival=" + arrival);
        showResponse(response);
    }

    private void cancelTicket() {
        String ticketId = JOptionPane.showInputDialog("Enter ticket ID:");
        String response = sendPostRequest("http://localhost:3000/cancel-ticket", "{\"ticketId\":\"" + ticketId + "\"}");
        showResponse(response);
    }

    private void addFlight() {
        String flightId = JOptionPane.showInputDialog("Enter flight ID:");
        String departure = JOptionPane.showInputDialog("Enter departure city:");
        String arrival = JOptionPane.showInputDialog("Enter arrival city:");
        String response = sendPostRequest("http://localhost:3000/add-flight", "{\"flightId\":\"" + flightId
                + "\",\"departure\":\"" + departure + "\",\"arrival\":\"" + arrival + "\"}");
        showResponse(response);
    }

    private void deleteFlight() {
        String flightId = JOptionPane.showInputDialog("Enter flight ID:");
        String response = sendPostRequest("http://localhost:3000/delete-flight", "{\"flightId\":\"" + flightId + "\"}");
        showResponse(response);
    }

    private String sendPostRequest(String urlString, String payload) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            return handleResponse(responseCode, response.toString());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String sendGetRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            return handleResponse(responseCode, response.toString());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleResponse(int responseCode, String response) {
        if (responseCode >= 200 && responseCode < 300) {
            return parseResponse(response);
        } else if (responseCode >= 400 && responseCode < 500) {
            return parseResponse(response);
        } else if (responseCode >= 500) {
            return "Server Error: " + response;
        } else {
            return "Unexpected Error: " + responseCode;
        }
    }

    private String parseResponse(String response) {
        try {
            String ori = extractJsonValue(response, "\"message\":");
            if (response.contains("\"success\":")) {
                String success = extractJsonValue(response, "\"success\":");
                String message = extractJsonValue(response, "\"message\":");

                if ("true".equals(success)) {
                    if (response.contains("\"ticketId\":")) {
                        if (response.contains("\"name\":")) {
                            String ticketId = extractJsonValue(response, "\"ticketId\":");
                            String name = extractJsonValue(response, "\"name\":");
                            String flightId = extractJsonValue(response, "\"flightId\":");
                            return String.format("Success: %s\nTicket ID: %s\nName: %s\nFlight ID: %s",
                                    message, ticketId,
                                    name, flightId);
                        } else {
                            String ticketId = extractJsonValue(response, "\"ticketId\":");
                            return "Success: " + message + "\nYour Ticket ID is: " + ticketId;
                        }
                    } else {
                        return "Success: " + message;
                    }
                } else {
                    return "Error: " + message;
                }
            } else if (response.contains("\"flights\":")) {
                String flightsArray = response.substring(response.indexOf("[") + 1, response.lastIndexOf("]"));
                String[] flightObjects = flightsArray.split("},");
                StringBuilder flightsList = new StringBuilder("Available Flights:\n");

                for (String flightObject : flightObjects) {
                    if (!flightObject.endsWith("}")) {
                        flightObject += "}";
                    }
                    String flightId = extractJsonValue(flightObject, "\"flightId\":");
                    String departure = extractJsonValue(flightObject, "\"departure\":");
                    String arrival = extractJsonValue(flightObject, "\"arrival\":");
                    flightsList.append(
                            String.format("Flight ID: %s, Departure: %s, Arrival: %s\n", flightId, departure, arrival));
                }
                return flightsList.toString();
            }
            return ori;
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private String extractJsonValue(String response, String key) {
        try {
            int startIndex = response.indexOf(key) + key.length();
            if (startIndex == -1)
                return "";
            int endIndex = response.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = response.indexOf("}", startIndex);
            }
            return response.substring(startIndex, endIndex).replace("\"", "").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void showResponse(String response) {
        JOptionPane.showMessageDialog(null, response, "Response", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new FlightTicketSystem();
    }
}
