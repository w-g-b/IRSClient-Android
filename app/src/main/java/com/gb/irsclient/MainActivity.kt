package com.gb.irsclient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import me.rosuh.filepicker.config.FilePickerManager
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {

    var isSelectFile = false
    var isRunCommand = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        sendBtn.setOnClickListener {
            if (remoteIpEdt.text.isEmpty() || remotePortEdt.text.isEmpty()) {
                Toast.makeText(this, "远程服务器IP或者端口为空!", Toast.LENGTH_SHORT).show()
            }
            if (localServerNameEdt.text.isEmpty()) {
                Toast.makeText(this, "本地服务器为空", Toast.LENGTH_SHORT).show()
            }
            if (sendCommandCb.isChecked) {
                if (!commandEdt.text.isEmpty()) {
                    Thread {
                        val socket =
                            Socket(remoteIpEdt.text.toString(), Integer.parseInt(remotePortEdt.text.toString()))
                        val bos = BufferedOutputStream(socket.getOutputStream())
                        bos.write(localServerNameEdt.text.toString().toByteArray())
                        bos.flush()
                        bos.write(Constant.MESSAGE_HEAD_BYTES)
                        bos.write(commandEdt.text.toString().toByteArray())
                        bos.write(Constant.MESSAGE_END_BYTES)
                        bos.flush()
                        socket.close()
                        runOnUiThread {
                            Toast.makeText(this, "发送命令: " + commandEdt.text.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                } else {
                    Toast.makeText(this, "命令为空", Toast.LENGTH_SHORT).show()
                }
            }
            if (sendFileCb.isChecked) {
                if (isSelectFile) {
                    Thread {
                        val socket =
                            Socket(remoteIpEdt.text.toString(), Integer.parseInt(remotePortEdt.text.toString()))
                        val bos = BufferedOutputStream(socket.getOutputStream())
                        bos.write(localServerNameEdt.text.toString().toByteArray())
                        bos.flush()
                        bos.write(Constant.MESSAGE_HEAD_BYTES)
                        Thread.sleep(1) //先发送头，防止数据出错

                        //传输文件
                        val bis = BufferedInputStream(FileInputStream(fileSelector.text.toString()))
                        var len = 0;
                        var bytes = ByteArray(1024)
                        len = bis.read(bytes)
                        while (len > 0) {
                            bos.write(bytes, 0, len)
                            len = bis.read(bytes)
                        }
                        bis.close()


                        bos.write(Constant.MESSAGE_END_BYTES)
                        bos.flush()
                        socket.close()
                        runOnUiThread {
                            Toast.makeText(this, "发送命令: " + commandEdt.text.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                } else {
                    Toast.makeText(this, "没有选中文件", Toast.LENGTH_SHORT).show()
                }
            }
        }
        clearSelectorBtn.setOnClickListener {
            fileSelector.text = "点击选择文件"
            clearSelectorBtn.visibility = View.GONE
            isSelectFile = false
        }
        fileSelector.setOnClickListener {
            //初始化fileSelector
            FilePickerManager.from(this).maxSelectable(1).forResult(FilePickerManager.REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    if (list != null) {
                        // do your work
                        isSelectFile = true
                        fileSelector.text = list.get(0)
                    }
                    clearSelectorBtn.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "没有选择任何东西~", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
