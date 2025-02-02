package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;

//This class performs the "Beacon Button Bopper" initialization functions
//This includes initializing and reading the color sensor
public class ButtonPushSystem {
    public enum BeaconColor {Red, Blue, Other}

    // Button Push System
    public Servo    sPushF = null,
                    sPushR = null,
                    sPushR2 = null;

    public ColorSensor  csPushR = null;

    static final double downPosition = 1.0f,
                        upPosition =  -1.0f;

    public void init(HardwareMap HWMap) {
        // Button Push System Init
        sPushF = HWMap.servo.get("sPushF");
        sPushR = HWMap.servo.get("sPushR");
        sPushR2 = HWMap.servo.get("sPushR2");
        sPushR.setDirection(Servo.Direction.REVERSE);
        sPushR2.setDirection(Servo.Direction.REVERSE);
        csPushR = HWMap.colorSensor.get("csPushR");
        csPushR.setI2cAddress(I2cAddr.create8bit(0x70) );
        csPushR.enableLed(false);
    }

    public BeaconColor PollRearSensor(){
        return (csPushR.blue() > csPushR.red() ? BeaconColor.Blue : BeaconColor.Red);
    }

    public void FrontPushOut(){
        sPushF.setPosition(1);
    }

    public  void FrontPushIn(){ sPushF.setPosition(-.80); }

    public void RearPushOut(){
        sPushR.setPosition(downPosition);
        sPushR2.setPosition(downPosition);
    }

    public void RearPushIn(){
        sPushR.setPosition(upPosition);
        sPushR2.setPosition(upPosition);
    }

    public void TeleopButtonPush(boolean FrontButton, boolean RearButton){
        if (FrontButton){
            FrontPushOut();
        }
        else{
            FrontPushIn();
        }

        if (RearButton){
            RearPushOut();
        }
        else{
            RearPushIn();
        }
    }

}
