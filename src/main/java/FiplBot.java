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
        }
    }


    private void handleAtletaCommand(String command, Long chatId) {
        String[] params = command.split("/atleta ");
        if (params.length == 2) {
            String name = params[1];
            String stats = getAthleteStats(name);
            sendResponse(chatId, stats);
        } else {
            sendResponse(chatId, "Formatta correttamente il comando: /atleta <nome cognome>");
        }
    }

    private String getAthleteStats(String name) {
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

}
