import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class FiplBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "poweritabot";
    }
    @Override
    public String getBotToken() {
        return "7780803693:AAGL8cfznuzTe2vecWTvZI0VnQDhFQmurfA";
    }

    private void sendResponse(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        try {
            execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        String text = msg.getText();
        Long chatId = msg.getChatId();

        if (text.equals("/start") || text.equals("/help")) {
            sendResponse(chatId, "");
        } else if (text.startsWith("/atleta")) {
            handleAtletaCommand(text, chatId);
        } else if (text.startsWith("/competizione")) {
            handleCompetizioneCommand(text, chatId);
        } else if (text.startsWith("/gare")) {
            handleGareCommand(text, chatId);
        } else if (text.startsWith("/me")) {
            handleMeCommand(chatId, text);
        }
    }


    private void handleAtletaCommand(String command, Long chatId) {
        String[] params = command.split("/atleta ");
        if (params.length == 2) {
            String name = params[1];
            String stats = getAtletaStats(name);
            sendResponse(chatId, stats);
        } else {
            sendResponse(chatId, "Formatta correttamente il comando: /atleta <nome cognome>");
        }
    }
    private String getAtletaStats(String name) {
        String res = "Gare di " + name + ":\n\n";
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT total_kg, place, goodlift, bodyweight_kg, age FROM Performance WHERE athlete_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();

                if (!rs.isBeforeFirst()) {
                    res += "Nessuna gara trovata per questo atleta.\n";
                }

                while (rs.next()) {
                    float total = rs.getFloat("total_kg");
                    String place = rs.getString("place");
                    float points = rs.getFloat("goodlift");
                    float bodyweight = rs.getFloat("bodyweight_kg");
                    int age = rs.getInt("age");

                    res += "Totale (kg): " + total
                            + "\n" + "Posizione: " + place
                            + "\n" + "IPF Points (GL): " + points
                            + "\n" + "Bodyweight (kg): " + bodyweight
                            + "\n" + "Età: " + age + "\n\n";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            res = "Errore durante il recupero delle statistiche.";
        }
        return res;
    }


    private void handleCompetizioneCommand(String command, Long chatId) {
        String[] params = command.split("/competizione ");
        if (params.length == 2) {
            String competitionName = params[1];
            String athletesStats = getCompetizioneStats(competitionName);
            sendResponse(chatId, athletesStats);
        } else {
            sendResponse(chatId, "Formatta correttamente il comando: /competizione <nome competizione>");
        }
    }
    private String getCompetizioneStats(String meetname) {
        String res = "";
        try (Connection conn = DB.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT date FROM Meet WHERE meetname = ? limit 1")) {
                stmt.setString(1, meetname);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                res += "Top 10 atleti nella gara: " + meetname + " svolta il giorno " + rs.getString("date") + "\n\n";
            }

            String sql = "SELECT P.athlete_name, P.place, P.goodlift " +
                    "FROM Performance P " +
                    "JOIN Meet M ON P.meet_id = M.id " +
                    "WHERE M.meetname = ? " +
                    "ORDER BY P.goodlift DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, meetname);
                ResultSet rs = stmt.executeQuery();
                if (!rs.isBeforeFirst()) {
                    res += "Nessun atleta trovato per questa competizione.\n";
                }
                int cont = 10;
                while (rs.next() && cont > 0) {
                    String athleteName = rs.getString("athlete_name");
                    String place = rs.getString("place");
                    float goodlift = rs.getFloat("goodlift");

                    res += "Atleta: " + athleteName
                            + "\n" + "IPF Points (GL): " + goodlift
                            + "\n" + "Posizione in categoria: " + place
                            + "\n\n";
                    cont--;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            res = "Errore durante il recupero delle statistiche.";
        }
        return res;
    }


    private void handleGareCommand(String command, Long chatId) {
        sendResponse(chatId, Scraper.getCompetitions());
    }


    private void handleMeCommand(Long chatId, String command) {
        try {
            String[] parts = command.split(" ");
            if (parts.length != 6) {
                sendResponse(chatId, "Formatta correttamente: /me <sesso(M/F)> <bodyweight> <squat> <bench> <deadlift>");
                return;
            }
            if (!parts[1].equalsIgnoreCase("M") && !parts[1].equalsIgnoreCase("F")) {
                sendResponse(chatId, "Sesso deve essere 'M' o 'F'.");
                return;
            }

            boolean sex = parts[1].equalsIgnoreCase("M");
            float bodyweight = Float.parseFloat(parts[2]);
            float squat = Float.parseFloat(parts[3]);
            float bench = Float.parseFloat(parts[4]);
            float deadlift = Float.parseFloat(parts[5]);
            float total = squat + bench + deadlift;

            try (Connection conn = DB.getConnection()) {
                String percentileQuery = "SELECT COUNT(*) AS weaker, " +
                        "(SELECT COUNT(*) FROM Performance) AS total " +
                        "FROM Performance WHERE goodlift < ?";
                PreparedStatement percentileStmt = conn.prepareStatement(percentileQuery);


                double points = getIPFPoints(total, bodyweight, sex);
                percentileStmt.setFloat(1, (float)points);
                ResultSet percentileRs = percentileStmt.executeQuery();

                float percentile = 0;
                if (percentileRs.next()) {
                    float weaker = percentileRs.getFloat("weaker");
                    float totalCount = percentileRs.getFloat("total");
                    percentile = (weaker / totalCount) * 100;
                }
                String strongestLift = getStrongestLift(squat, bench, deadlift);
                String response = String.format(
                        "Le tue statistiche:\n" +
                                "- Totale (KG): %.2f kg\n" +
                                "- Più forte del: %.2f%%" + " degli atleti\n" +
                                "- IPF Points: %.2f\n" +
                                "- Alzata più competitiva: %s (%.2f kg)",
                        total, percentile, points, strongestLift,
                        strongestLift.equals("Squat") ? squat : strongestLift.equals("Bench") ? bench : deadlift
                );
                sendResponse(chatId, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(chatId, "An error occurred while processing your performance.");
        }
    }
    public static double getIPFPoints(double total, double bw, boolean gender) {
        // coefficienti
        double[] men = {1199.72839, 1025.18162, 0.009210};
        double[] women = {610.32796, 1045.59282, 0.03048};

        double res = 0.0;
        if (gender == true) {
            double denom = men[0] - (men[1] * Math.exp(-1.0 * men[2] * bw));
            res = (denom == 0) ? 0 : Math.max(0, total * 100 / denom);
        }
        else {
            double denom = women[0] - (women[1] * Math.exp(-1.0 * women[2] * bw));
            res = (denom == 0) ? 0 : Math.max(0, total * 100 / denom);
        }

        return res;
    }
    private String getStrongestLift(float squat, float bench, float deadlift) {
        // Proporzioni standard (approssimative)
        float squatStandard = 4f;
        float benchStandard = 3f;
        float deadliftStandard = 5f;
        float totalProportion = squatStandard + benchStandard + deadliftStandard;

        float expectedSquat = (squatStandard / totalProportion) * (squat + bench + deadlift);
        float expectedBench = (benchStandard / totalProportion) * (squat + bench + deadlift);
        float expectedDeadlift = (deadliftStandard / totalProportion) * (squat + bench + deadlift);

        float squatExcess = squat / expectedSquat;
        float benchExcess = bench / expectedBench;
        float deadliftExcess = deadlift / expectedDeadlift;

        if (squatExcess >= benchExcess && squatExcess >= deadliftExcess) {
            return "Squat";
        } else if (benchExcess >= squatExcess && benchExcess >= deadliftExcess) {
            return "Bench";
        } else {
            return "Deadlift";
        }
    }
}