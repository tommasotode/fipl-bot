import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@SuppressWarnings("deprecation")
public class FiplBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            switch (command.split(" ")[0]) {
                case "/start":
                case "/help":
                    sendResponse(chatId, "Welcome! Here are the commands:\n/start\n/help\n/me <data>\n...");
                    break;
                case "/me":
                    // Call method to process user performance analysis
                    break;
                case "/atleta":
                    // Call method to fetch athlete data
                    break;
                case "/competizione":
                    // Call method to fetch competition data
                    break;
                case "/gare":
                    // Call method to scrape and display upcoming competitions
                    break;
                default:
                    sendResponse(chatId, "Unknown command. Use /help for a list of commands.");
            }
        }
    }

    private void sendResponse(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "poweritabot";
    }

    @Override
    public String getBotToken() {
        return "7780803693:AAGL8cfznuzTe2vecWTvZI0VnQDhFQmurfA";
    }
}
