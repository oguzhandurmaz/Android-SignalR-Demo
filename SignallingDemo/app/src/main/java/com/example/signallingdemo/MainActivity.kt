package com.example.signallingdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var hubConnection: HubConnection


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Get Hub Object
        hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5000/signalhub")
            .build()
        //Socket Connection onClosed
        hubConnection.onClosed {
            Log.d("SignalR", "onClosed")
        }


        //Listeners
        hubConnection.on(
            "PeerList",
            { message ->
                val json = JSONArray(message)
                val user = json.getJSONObject(0)
                Log.d("SignalR", "PeerList $json")
            },
            String::class.java
        )
        hubConnection.on(
            "PeerConnected", { message ->
                val userList = JSONArray(message)
                Log.d("SignalR", "Peer Connected $userList")

            }, String::class.java
        )
        hubConnection.on(
            "Connect", { message ->
                Log.d("SignalR", "Connected $message")

            }, String::class.java
        )
        //

        //Connect
        btn_connect.setOnClickListener {
            connectToSocket()
        }

        //Send
        btn_send.setOnClickListener {
            signalRConnect()
        }

    }

    //SignarR Connect to Socket
    private fun connectToSocket() {
        Log.d("SignalR", "Connect to Socket")
        //Connect to Server - Socket
        try {
            hubConnection.start().blockingAwait()
            if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                btn_send.isEnabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //SignarR General Send Function
    private fun signalRSend(method: String, message: JSONObject? = null) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            message?.let {
                hubConnection.send(method, message.toString())

            } ?: kotlin.run {
                hubConnection.send(method)
            }
        }
    }

    //Send Uniq UserName
    private fun signalRConnect() {
        Log.d("SignalR", "Send Connect")
        val user = JSONObject()
        user.put("Name", "user2")
        signalRSend("Connect", user)

    }

    override fun onDestroy() {
        hubConnection.stop()
        super.onDestroy()


    }
}
