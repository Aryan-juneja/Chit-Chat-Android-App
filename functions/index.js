import * as functions from "firebase-functions";
import axios from "axios";

// Firebase Cloud Function to send FCM Notification
export const sendNotification = functions.https.onRequest(async (req, res) => {
  const { fcmToken, title, body } = req.body;
  const message = {
    message: {
      token: fcmToken,
      notification: {
        title: title,
        body: body,
      },
    },
  };

  try {
    const response = await axios.post(
      "https://fcm.googleapis.com/v1/projects/whatsappclone-55ec3/messages:send",
      message,
      {
        headers: {
          Authorization: "Bearer YOUR_SERVER_KEY", // Use Bearer token format
          "Content-Type": "application/json",
        },
      }
    );
    res.status(200).send(response.data);
  } catch (error) {
    console.error("Error sending notification:", error);
    res.status(500).send("Error sending notification");
  }
});
