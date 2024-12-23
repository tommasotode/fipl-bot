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
}
