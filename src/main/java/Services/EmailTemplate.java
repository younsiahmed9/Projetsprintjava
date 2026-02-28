package Services;

public class EmailTemplate {

    public static String getHeader() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fb; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #182d88, #2c5aa0); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 32px; }
                    .content { padding: 30px; }
                    .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px; border-top: 1px solid #e0e0e0; }
                    .button { background: #182d88; color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; display: inline-block; font-weight: bold; }
                    .button:hover { background: #2c5aa0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏦 FinTrack</h1>
                    </div>
                    <div class="content">
        """;
    }

    public static String getFooter() {
        return """
                    </div>
                    <div class="footer">
                        <p>© 2026 FinTrack - Tous droits réservés</p>
                        <p>Ce message est automatique, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
        """;
    }
}