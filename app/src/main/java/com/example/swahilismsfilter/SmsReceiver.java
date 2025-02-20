package com.example.swahilismsfilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, "3gpp");
                String messageBody = sms.getMessageBody();
                String sender = sms.getOriginatingAddress();

                // Send the message to a service for processing
                Intent serviceIntent = new Intent(context, SmsProcessingService.class);
                serviceIntent.putExtra("message", messageBody);
                context.startService(serviceIntent);
            }
        }
    }
}