package com.manyox.msprinter.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.graphics.Bitmap;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
 
import com.printsdk.cmd.PrintCmd;
import com.printsdk.usbsdk.UsbDriver;

public class UsbPrinter {
	private static final String TAG = "UsbPrinter";
	private static final String ACTION_USB_PERMISSION =  "com.usb.sample.USB_PERMISSION";
	private Context mContext;
	private UsbManager mUsbManager;
	UsbDriver mUsbDriver = null; 
	private UsbDevice m_Device;
	String iline = "4";

	public UsbPrinter(Context context) {
		this.mContext = context;
		OpenDevice();
	}
	
	// 常规设置
	private void setClean(){
		mUsbDriver.write(PrintCmd.SetClean());// 清除缓存,初始化
	}
	
	public boolean OpenDevice(){

		boolean blnRtn = false;
		if(mUsbDriver!=null)
		{
			if(mUsbDriver.isConnected())
				return true;
			else
				mUsbDriver.closeUsbDevice();
		}
		
		mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);				
		mUsbDriver = new UsbDriver(mUsbManager, mContext);
		try {
			Log.d(TAG,"mUsbManager.length1:"+mUsbManager.getDeviceList().size());
			if (!mUsbDriver.isConnected()) {
				Log.d(TAG,"!mUsbDriver.isConnected");
				for (UsbDevice device : mUsbManager.getDeviceList().values()) {
					Log.i(TAG, device.getDeviceName()+"--->device: VID = " + device.getVendorId() + "  PID = " + device.getProductId());	
					if((device.getProductId()==8211 && device.getVendorId()==1305)
							||(device.getProductId()==8213 && device.getVendorId()==1305)) {
						Log.d(TAG,"find usbprinter pid vid!");
						if (!mUsbManager.hasPermission(device)) {
							Log.d(TAG,"!mUsbManager.hasPermission(device)");
							PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, 
			                        new Intent(ACTION_USB_PERMISSION), 0);
							mUsbManager.requestPermission(device, mPermissionIntent);
							ToolUtil.upgradedirPermission(device.getDeviceName());
							int addPermission = 0;
							while(!mUsbManager.hasPermission(device)&&addPermission<4){
								addPermission++;
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									//Log.i(TAG, e);
								}
								mUsbManager.requestPermission(device, mPermissionIntent);
								ToolUtil.upgradedirPermission(device.getDeviceName());
							}
							Log.i(TAG, device.getDeviceName()+"--->device: VID = " + device.getVendorId() + "  PID = " + device.getProductId()+" permission "+mUsbManager.hasPermission(device));	
						} 
						blnRtn = mUsbDriver.usbAttached(device);
						if (blnRtn == false) {
							break;
						}
						blnRtn = mUsbDriver.openUsbDevice(device);
						Log.d(TAG, "openUsbDevice ");
						// 打开设备
						if (blnRtn) {
							//setClean();// 清理缓存，初始化
							Log.d(TAG, "openUsbDevice Success");
							m_Device=device;
							break;
						} else {
							Log.d(TAG, "openUsbDevice Failed");
							break;
						}
					}
				}
			} 
			else {
				blnRtn = true;
				Log.d(TAG,"mUsbDriver.isConnected");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return blnRtn;
	}
	    
	public boolean CloseDevice(){
		//mUsbDriver.closeUsbDevice();
		return true;
	}
	
//	public int PrintPDF(String filePath, boolean bCut) {
//		if (!mUsbDriver.isConnected())
//			return -1;
//		int[] data;
//		if (bCut){
//			data = com.printsdk.utils.PrintUtils.getPdfPrintDataCut(filePath,
//					1, 640, 640);
//		}else {
//			data = com.printsdk.utils.PrintUtils.getPdfPrintData(filePath, 1,
//					640, 640);
//		}
//		int iCutHeight = com.printsdk.utils.PrintUtils.getCutHeight();
//		Log.d(TAG, "iCutHeight=" + iCutHeight);
//		byte[] writeBytes = PrintCmd.PrintDiskImagefile(data, 640, iCutHeight);
//		Log.i(TAG, "filePath = " + filePath + ";data length  = " + data.length
//				+ ",writeBytes length = " + writeBytes.length);
//		return mUsbDriver.write(writeBytes);
//	}
	
	public int PirntFeedBack(int iValue){
		if (!mUsbDriver.isConnected())return -1;
		return mUsbDriver.write(com.printsdk.utils.PrintUtils.PrintFeedBack(iValue));
	}
	
	public int PirntFeedForward(int iValue){
		if (!mUsbDriver.isConnected())return -1;
		return mUsbDriver.write(com.printsdk.utils.PrintUtils.PrintFeedForward(iValue));
	}
	
	public int PrintMarkcutpaper(){
		if (!mUsbDriver.isConnected())return -1;
		return mUsbDriver.write(com.printsdk.utils.PrintUtils.PrintMarkcutpaper());
	}
	
	// 检测打印机状态
	public int getPrinterStatus() {
		int iRet = -1;

		byte[] bRead1 = new byte[1];
		byte[] bWrite1 = PrintCmd.GetStatus1();		
		if(mUsbDriver.read(bRead1,bWrite1,m_Device)>0)
		{
			iRet = PrintCmd.CheckStatus1(bRead1[0]);
		}
		
		if(iRet!=0)
			return iRet;
		
		byte[] bRead2 = new byte[1];
		byte[] bWrite2 = PrintCmd.GetStatus2();		
		if(mUsbDriver.read(bRead2,bWrite2,m_Device)>0)
		{
			iRet = PrintCmd.CheckStatus2(bRead2[0]);
		}

		if(iRet!=0)
			return iRet;
		
		byte[] bRead3 = new byte[1];
		byte[] bWrite3 = PrintCmd.GetStatus3();		
		if(mUsbDriver.read(bRead3,bWrite3,m_Device)>0)
		{
			iRet = PrintCmd.CheckStatus3(bRead3[0]);
		}

		if(iRet!=0)
			return iRet;
		
		byte[] bRead4 = new byte[1];
		byte[] bWrite4 = PrintCmd.GetStatus4();		
		if(mUsbDriver.read(bRead4,bWrite4,m_Device)>0)
		{
			iRet = PrintCmd.CheckStatus4(bRead4[0]);
		}

		
		return iRet;
	}
	
	public static int CheckStatus_TS(byte[] b_recv) {
		if ((b_recv[1] & 0x04) == 0x04)// 抬杆打开
			return 103;

		if ((b_recv[3] & 0x60) == 0x60)// 缺纸
			return 105;

		if ((b_recv[2] & 0x04) == 0x04)// 黑标错误
			return 107;

		if ((b_recv[2] & 0x08) == 0x08)// 切刀错误
			return 108;

		if ((b_recv[2] & 0x40) == 0x40)// 过温错误
			return 109;

		if ((b_recv[3] & 0x01) == 0x01)// 带未安装
			return 110;

		if ((b_recv[3] & 0x80) == 0x80)// 色带过少停止打印
			return 112;

		if ((b_recv[3] & 0x0C) == 0x0C)// 色带将尽
			return 111;

		if ((b_recv[0] & 0x40) == 0x40)// 按键按下
			return 102;

		return 0;
	}

	
	// 检测打印机状态_TS101
	public int getPrinterStatus_TS() {
		int iRet = -1;
		byte[] bRead = new byte[4];
		byte[] bWrite = PrintCmd.GetStatus();

		if (mUsbDriver.read(bRead, bWrite, m_Device) > 0) {
			iRet = CheckStatus_TS(bRead);
		}
		return iRet;
	}

	public void printtest() {
		mUsbDriver.write(PrintCmd.SetSizetext(1, 1), 1);
		mUsbDriver.write(PrintCmd.SetLeftmargin(90));
		mUsbDriver.write(PrintCmd.SetLinespace(100));
		mUsbDriver.write(PrintCmd.PrintString("====培训模式====", 0));

		mUsbDriver.write(PrintCmd.SetLinespace(10));
		mUsbDriver.write(PrintCmd.SetSizetext(0, 0), 1);
		mUsbDriver.write(PrintCmd.PrintString("Trx:242     3003 A001 2019/03/21", 0));

		mUsbDriver.write(PrintCmd.PrintString("=================================", 0));
		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("商品名" + "", 16, ' ')
				+ CommUtil.fillRightString("数量" + "", 12, ' ')
				+ CommUtil.fillRightString("金额" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString("会员卡：186****2998", 0));

		mUsbDriver.write(PrintCmd.PrintString("纸杯16安士1*50", 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("1876470" + "", 16, ' ')
				+ CommUtil.fillRightString("1" + "", 12, ' ')
				+ CommUtil.fillRightString("16.00" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("会员折扣" + "", 16, ' ')
				+ CommUtil.fillRightString("" + "", 12, ' ')
				+ CommUtil.fillRightString("-1.6" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString("纸杯16安士1*50", 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("1876470" + "", 16, ' ')
				+ CommUtil.fillRightString("1" + "", 12, ' ')
				+ CommUtil.fillRightString("16.00" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("会员折扣" + "", 16, ' ')
				+ CommUtil.fillRightString("" + "", 12, ' ')
				+ CommUtil.fillRightString("-1.6" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString("支付宝支付优惠：0.30", 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("收款：支付宝支付" + "", 16, ' ')
				+ CommUtil.fillRightString("" + "", 12, ' ')
				+ CommUtil.fillRightString("28.8" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString("A00Q190321A0 01024 20102 0101", 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("实收：" + "", 16, ' ')
				+ CommUtil.fillRightString("" + "", 12, ' ')
				+ CommUtil.fillRightString("28.50" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("优惠总计：" + "", 16, ' ')
				+ CommUtil.fillRightString("" + "", 12, ' ')
				+ CommUtil.fillRightString("3.20" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString((CommUtil.fillRightString("已买商品总数：" + "", 16, ' ')
				+ CommUtil.fillRightString("2" + "", 12, ' ')
				+ CommUtil.fillRightString("" + "", 5, ' ')), 0));

		mUsbDriver.write(PrintCmd.PrintString("本次积分：0", 0));

		mUsbDriver.write(PrintCmd.PrintString("============16:12:08============", 0));
		mUsbDriver.write(PrintCmd.PrintString("=================================", 0));

		mUsbDriver.write(PrintCmd.PrintString("开票凭据     交易金额：28.50", 0));

		mUsbDriver.write(PrintCmd.PrintString("2019-03-21     A001 Trx:242", 0));

		//条形码
		mUsbDriver.write(PrintCmd.PrintFeedline(1));
		mUsbDriver.write(PrintCmd.SetLinespace(90));
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.SetLeftmargin(15));
		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 1, 2, 9, "00A00QP30920190318000001"));
//
//		mUsbDriver.write(PrintCmd.SetSizetext(1,1),1);
//		mUsbDriver.write(PrintCmd.SetLinespace(90));
//		mUsbDriver.write(PrintCmd.SetAlignment(1));
//        mUsbDriver.write(PrintCmd.SetLeftmargin(0));
//		mUsbDriver.write(PrintCmd.PrintString("赠品领用券", 0));
//
//		mUsbDriver.write(PrintCmd.SetSizetext(0,0),1);
//		mUsbDriver.write(PrintCmd.SetLinespace(0));
//		mUsbDriver.write(PrintCmd.PrintString("感谢您来华润万家购物", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("自助测试81", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("测试赠品券1", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("测试赠品券2", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("有效日期20180619 至 20991231", 0));
//
//		mUsbDriver.write(PrintCmd.PrintFeedline(1));
//		mUsbDriver.write(PrintCmd.SetLinespace(20));
//		mUsbDriver.write(PrintCmd.SetAlignment(1));
//		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 1, 2, 10, "0123456789001294789"));
//
//		mUsbDriver.write(PrintCmd.SetSizetext(0,1),1);
//		mUsbDriver.write(PrintCmd.SetAlignment(0));
//		mUsbDriver.write(PrintCmd.SetLeftmargin(90));
//		mUsbDriver.write(PrintCmd.PrintString("使用规则", 0));
//
//		mUsbDriver.write(PrintCmd.SetSizetext(0,0),1);
//		mUsbDriver.write(PrintCmd.PrintString("该券仅限南区使用", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("该券不能用于购买任何产品", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("该券仅作为领取相应赠品的凭证", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("该券仅限使用一次，复印无效", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("请在有效期内使用，逾期自动作废", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("请妥善保管，遗失不补", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString("对于任何伪造行为将追究其法律责任", 0));
//
//		mUsbDriver.write(PrintCmd.PrintString(" 20190321A00Q   A001 242 3003", 0));
//
//		二维码
		String qrcode1 = "1";
		String qrcode2 = "2";
		Bitmap inputBmp1, inputBmp2;
		inputBmp1 = QrCodeUtil.getQRcode(qrcode1, 200, 200);
		inputBmp2 = QrCodeUtil.getQRcode(qrcode2, 200, 200);
		if (inputBmp1 == null)
			return;
		Bitmap bm = QrCodeUtil.getSinglePic(inputBmp1);
		Bitmap bm2 = QrCodeUtil.getSinglePic(inputBmp2);
		Bitmap mergeBitmap = QrCodeUtil.mergeBitmap_LR(bm, bm2, true);
		int[] data1 = QrCodeUtil.getPixelsByBitmap(mergeBitmap);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data1, mergeBitmap.getWidth(), mergeBitmap.getHeight()));

		setFeedCut(0, Integer.valueOf(iline));
	}

	// 指定设备：走纸换行、切纸类型
	private void setFeedCut(int iMode, UsbDevice usbDev, int iline) {
		mUsbDriver.write(PrintCmd.PrintFeedline(iline), usbDev);      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode), usbDev);  // 切纸类型
	}

	// 固定---走纸换行数量、切纸类型
	private void setFeedCut(int iMode) {
		mUsbDriver.write(PrintCmd.PrintFeedline(5));      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode));  // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());           // 清除缓存,初始化
	}

	// 指定---走纸换行数量、切纸类型
	private void setFeedCut(int iMode, int num) {
//		if(iMode == 0){
//			T.showShort(MainActivity.this, "全切："  + cutter);
//		}else{
//			T.showShort(MainActivity.this, "半切："  + cutter);
//		}
		mUsbDriver.write(PrintCmd.PrintFeedline(num));   // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode)); // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());           // 清除缓存,初始化
	}

	// 黑标切纸
	private void setFeedMarkCut(int iMode, int num) {
		mUsbDriver.write(PrintCmd.PrintFeedline(num));   // 走纸换行
		mUsbDriver.write(PrintCmd.PrintMarkposition());  // 定位黑标
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode)); // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());           // 清除缓存,初始化
	}
	
	/*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
//	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//				UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//				if((device.getProductId()==8211 && device.getVendorId()==1305)
//						||(device.getProductId()==8213 && device.getVendorId()==1305)){
//					Log.d(TAG,"close");	
//					mUsbDriver.closeUsbDevice(device);
//					Log.d(TAG,"close end");
//				}
//			}
//		}
//	};
}
	
