package com.ysn.examplenearbyapi

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v4.util.SimpleArrayMap
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.io.*

class MainActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger {

    private lateinit var endpointId: String
    private lateinit var notificationManager: NotificationManager
    private lateinit var incomingPayloads: SimpleArrayMap<Long, NotificationCompat.Builder>
    private lateinit var outgoingPayloads: SimpleArrayMap<Long, NotificationCompat.Builder>
    private var filename = ""
    private val mConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            info { "onConnectionResult" }
            showToast("onConnectionResult")
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    showToast("We're connected! Can now start sending and receiving data")
                    this@MainActivity.endpointId = endpointId
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    showToast("The connection was rejected by one or both sides.")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    showToast("The connection broke before it was able to be accepted.")
                }
                else -> {
                    /* nothing to do in here */
                }
            }
        }

        override fun onDisconnected(endpoint: String) {
            info { "onDisconnected" }
            showToast("onDisconnected")
        }

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            info { "onConnectionInitiated" }
            showToast("onConnectionInitiated")

            // Automatically accept the connection on both sides.
            /*Nearby.getConnectionsClient(this@MainActivity).acceptConnection(endpoint, object : PayloadCallback() {
                override fun onPayloadReceived(p0: String, p1: Payload) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })*/

            // Authenticate a connection
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("Accept connection to ${connectionInfo.endpointName}")
                    .setMessage("Confirm the code ${connectionInfo.authenticationToken} is also displayed on other device")
                    .setPositiveButton("Accept") { dialogInterface, which ->
                        // The user confirmed, so we can accept the connection.
                        Nearby.getConnectionsClient(this@MainActivity).acceptConnection(endpointId, object : PayloadCallback() {
                            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                                /*if (payload.type == Payload.Type.BYTES) {
                                    // No need to track progress for bytes.
                                    return
                                }

                                // Build and start showing the notification.
                                val notification = buildNotification(payload, true)
                                notificationManager.notify(payload.id.toInt(), notification?.build())

                                // Add it to the tracking list so we can update it.
                                incomingPayloads.put(payload.id, notification)*/

                                /*if (payload.type == Payload.Type.BYTES) {
                                    val message = deserialize(payload.asBytes()!!)
                                    info { "message: $message" }
                                }*/

                                if (payload.type == Payload.Type.BYTES) {
                                    val data = deserialize(payload.asBytes()!!).toString()
                                    if (data.contains("filename:")) {
                                        this@MainActivity.filename = data.replace("filename: ", "").trim()
                                    }
                                } else if (payload.type == Payload.Type.FILE) {
                                    val fileImage = payload.asFile()?.asJavaFile()
                                    val dirImage = File(Environment.getExternalStorageDirectory().path + "/t2t_dev/partner/")
                                    if (!dirImage.exists()) {
                                        dirImage.mkdirs()
                                    }
                                    info { "dirImage: $dirImage" }
                                    info { "fileImage: ${this@MainActivity.filename}" }
                                    fileImage?.renameTo(File(dirImage, this@MainActivity.filename))
                                    info { "Image has been written" }
                                } else if (payload.type == Payload.Type.STREAM) {

                                }
                            }

                            override fun onPayloadTransferUpdate(payloadId: String, update: PayloadTransferUpdate) {
                                /*var notification = NotificationCompat.Builder(this@MainActivity)
                                if (incomingPayloads.containsKey(payloadId)) {
                                    notification = incomingPayloads.get(payloadId)
                                    if (update.status != PayloadTransferUpdate.Status.IN_PROGRESS) {
                                        // This is the last update, so we longer need to keep track of this notification.
                                        incomingPayloads.remove(payloadId)
                                    } else if (outgoingPayloads.containsKey(payloadId)) {
                                        notification = outgoingPayloads.get(payloadId)
                                        if (update.status != PayloadTransferUpdate.Status.IN_PROGRESS) {
                                            // This is the last update, so we no longer need t keep track of this notification.
                                            outgoingPayloads.remove(payloadId)
                                        }
                                    }

                                    when (update.status) {
                                        PayloadTransferUpdate.Status.IN_PROGRESS -> {
                                            val size = update.totalBytes.toInt()
                                            if (size == -1) {
                                                // This is a stream payload, so we don't need to update anything at this point.
                                                return
                                            }
                                            notification.setProgress(size, update.bytesTransferred.toInt(), false)
                                        }
                                        PayloadTransferUpdate.Status.SUCCESS -> {
                                            // SUCCESS always means that we transferred 100%.
                                            notification.setProgress(100, 100, false)
                                                    .setContentText("Transfer complete!")
                                        }
                                        PayloadTransferUpdate.Status.FAILURE -> {
                                            notification.setProgress(0, 0, false)
                                                    .setContentText("Transfer failed")
                                        }
                                        else -> {
                                            *//* nothing to do in here *//*
                                        }
                                    }
                                }
                                notificationManager.notify(payloadId.toInt(), notification.build())*/
                            }
                        })
                    }
                    .setNegativeButton(android.R.string.cancel, { dialogInterface, which ->
                        // The user canceled, so we should reject the connection.
                        Nearby.getConnectionsClient(this@MainActivity).rejectConnection(endpointId)
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        incomingPayloads = SimpleArrayMap()
        outgoingPayloads = SimpleArrayMap()

        doRequestPermission()
        initListeners()
    }

    private fun doRequestPermission() {
        val permissions = mutableListOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        Dexter.withActivity(this)
                .withPermissions(permissions)
                .withListener(object : PermissionListener, MultiplePermissionsListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        /* nothing to do in here */
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                        /* nothing to do in here */
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        /* nothing to do in here */
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        /* nothing to do in here */
                    }

                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        /* nothing to do in here */
                    }
                })
                .check()
    }

    private fun initListeners() {
        button_start_server.setOnClickListener(this)
        button_stop_server.setOnClickListener(this)
        button_connect_server.setOnClickListener(this)
        button_disconnect_server.setOnClickListener(this)
        button_send_message.setOnClickListener(this)
        button_send_file.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_start_server -> {
                startAdvertising()
            }
            R.id.button_stop_server -> {
                Nearby.getConnectionsClient(this).stopAdvertising()
            }
            R.id.button_connect_server -> {
                startDiscovery()
            }
            R.id.button_disconnect_server -> {
                Nearby.getConnectionsClient(this).disconnectFromEndpoint(endpointId)
                Nearby.getConnectionsClient(this).stopDiscovery()
            }
            R.id.button_send_message -> {
                val payloadMessage = Payload.fromBytes(serialize("Hello world"))
                /*sendPayload(endpointId, payloadMessage)*/
                Nearby.getConnectionsClient(this).sendPayload(endpointId, payloadMessage)
            }
            R.id.button_send_file -> {
                val fileImage = File(Environment.getExternalStorageDirectory().path + "/t2t_dev/partner/testing.jpg")
                info { "fileImage: $fileImage" }
                val fileName = "filename: " + fileImage.name
                val payloadBytes = Payload.fromBytes(serialize(fileName))
                val payloadFile = Payload.fromFile(fileImage)
                Nearby.getConnectionsClient(this).sendPayload(endpointId, payloadBytes)
                Nearby.getConnectionsClient(this).sendPayload(endpointId, payloadFile)
            }
            else -> {
                /* nothing to do in here */
            }
        }
    }

    private fun startDiscovery() {
        Nearby
                .getConnectionsClient(this).startDiscovery(
                        BuildConfig.APPLICATION_ID,
                        object : EndpointDiscoveryCallback() {
                            override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
                                info { "An endpoint was found" }
                                info { "endpoint name: ${discoveredEndpointInfo.endpointName} and serviceId: ${discoveredEndpointInfo.serviceId}" }
                                showToast("An endpoint was found")
                                Nearby
                                        .getConnectionsClient(this@MainActivity).requestConnection(Build.MANUFACTURER, endpointId, mConnectionLifecycleCallback)
                                        .addOnSuccessListener {
                                            // We successfully requested a connection. Now both sides
                                            // must accept before the connection is established.
                                        }
                                        .addOnFailureListener {
                                            // Nearby connection failed to request the connection.
                                        }
                            }

                            override fun onEndpointLost(p0: String) {
                                info { "A previously discovered endpoint has gone away" }
                                info { "endpoint name: $p0" }
                                showToast("A previously discovered endpoint has gone away")
                            }
                        },
                        DiscoveryOptions(Strategy.P2P_STAR)
                )
                .addOnSuccessListener {
                    info { "We're discovering" }
                    showToast("We're discovering")
                }
                .addOnFailureListener {
                    info { "We were unable to start discovering" }
                    showToast("We were unable to start discovering")
                }
    }

    private fun startAdvertising() {
        Nearby
                .getConnectionsClient(this).startAdvertising(
                        Build.MANUFACTURER,
                        BuildConfig.APPLICATION_ID,
                        mConnectionLifecycleCallback,
                        AdvertisingOptions(Strategy.P2P_STAR)
                )
                .addOnSuccessListener {
                    info { "We're adverstising" }
                    showToast("We're advertising")
                }
                .addOnCanceledListener {
                    info { "We were unable to start advertising" }
                    showToast("We were unable to start advertising")
                }
    }

    private fun showToast(message: String) {
        toast(message)
    }

    private fun sendPayload(endpointId: String, payload: Payload) {
        if (payload.type == Payload.Type.BYTES) {
            // No need to track progress for bytes.
            return
        }

        // Build and start showing the notification.
        val notification = buildNotification(payload, false)
        notificationManager.notify(payload.id.toInt(), notification?.build())

        // Add it to the tracking list so we can update it.
        outgoingPayloads.put(payload.id, notification)
    }

    private fun buildNotification(payload: Payload, isIncoming: Boolean): NotificationCompat.Builder? {
        val notification = NotificationCompat.Builder(this)
                .setContentTitle(if (isIncoming) "Receiving..." else "Sending...")
        var size = payload.asBytes()!!.size
        var indeterminate = false
        if (size == -1) {
            // This is a stream payload, so we don't know the size ahead of time.
            size = 100
            indeterminate = true
        }
        notification.setProgress(size, 0, indeterminate)
        return notification
    }

    private fun serialize(data: Any): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        // transform data to stream and then to byte array
        objectOutputStream.writeObject(data)
        objectOutputStream.flush()
        objectOutputStream.close()
        return byteArrayOutputStream.toByteArray()
    }

    private fun deserialize(bytes: ByteArray): Any {
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        return objectInputStream.readObject()
    }

}
