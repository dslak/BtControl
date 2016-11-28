
#include <SoftwareSerial.h>

#define btTx 10
#define btRx 11

#define btRpm 4
#define btPres A3
#define btFuel A4
#define btBatt A5
#define btTemp A2
#define btCont A1

#define rlCons 2
#define rlStart 13
#define rlExtra 12


SoftwareSerial BT(btTx, btRx); 

  char strc[1];
  String str;
  int strp;


void setup(){

  pinMode(btRpm,INPUT);
  pinMode(btCont,INPUT);
  
  pinMode(rlCons,OUTPUT);
  pinMode(rlStart,OUTPUT);
  pinMode(rlExtra,OUTPUT);
  
  Serial.begin(9600);
  BT.begin(9600);

  
}




void loop(){

  str="";
  strp=0;

  // From App to Arduino
  while (BT.available() > 0) {
    strc[0]=BT.read();
    str=String(str + strc[0]);
    strp++;
  }

  if(strp>0){
    Serial.println(str);
    
    if(str == "CONSOLE 0"){
      digitalWrite(rlCons,LOW);
    }
    if(str == "CONSOLE 1"){
      digitalWrite(rlCons,HIGH);
    }
    if(str == "MOTOR 0"){
      digitalWrite(rlCons,LOW);
    }
    if(str == "MOTOR 1"){
      digitalWrite(rlCons,HIGH);
    }
      
      
    //Serial.println();
  }


  str="";

    str=String(str + pulseIn(btRpm,LOW) + " "
                   + map(analogRead(btPres),0,1023,0,8) + " "
                   + map(analogRead(btFuel),0,1023,0,100) + " "
                   + map(analogRead(btBatt),0,1023,0,12) + " "
                   + map(analogRead(btTemp),0,1023,0,130) + " "
                   + digitalRead(btCont));
                   
    delay(200);

    BT.println(str);
    Serial.println(str);

  
}




