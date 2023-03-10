package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CanvasController {
    @Autowired
    protected SimpMessagingTemplate template;
    @Autowired
    protected Canvas canvas;

    @MessageMapping("/paint")
    @SendTo("/topic/canvas")
    public Canvas paintBoard(PaintMessage paintMessage) {
        int x = paintMessage.getX();
        int y = paintMessage.getY();
        String color = paintMessage.getColor();

        String currentPlayer = canvas.getCurrentPlayer();

        if (!color.equals(currentPlayer)) {
            // It's not this player's turn
            String errorMessage = "It's not your turn.";
            template.convertAndSend("/topic/error", "{\"error\": \"" + errorMessage + "\"}");
            return canvas;
        }

        // Paint the board
        boolean paintSuccessful = canvas.paint(x, y, color);

        if (paintSuccessful) {
            // Check if the game is over
            if (canvas.checkWin(color)) {
                template.convertAndSend("/topic/gameOver", color + " wins!");
                canvas.resetBoard();
                canvas.setCurrentPlayer(canvas.getPLAYER_X());
            } else if (canvas.checkDraw()) {
                template.convertAndSend("/topic/gameOver", "Draw!");
                canvas.resetBoard();
                canvas.setCurrentPlayer(canvas.getPLAYER_X());
            } else {
                // Update the current player
                canvas.setCurrentPlayer((color.equals(canvas.getPLAYER_X())) ? canvas.getPLAYER_O() : canvas.getPLAYER_X());
            }
        } else {
            // The paint was not valid
            template.convertAndSend("/topic/error", "Invalid paint.");
        }

        return canvas;
    }

    @SubscribeMapping("/canvas")
    public Canvas sendInitialCanvas() {
        canvas.resetBoard();
        canvas.setCurrentPlayer(canvas.getPLAYER_X());
        return canvas;
    }
}
