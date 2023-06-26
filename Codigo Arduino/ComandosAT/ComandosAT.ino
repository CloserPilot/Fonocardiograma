#include <SoftwareSerial.h>  
SoftwareSerial BT(10,11);   // Pin TX y RX del fono
 
void setup(){
  BT.begin(38400);          // Inicializacion del puerto Serie BT
  Serial.begin(38400);      // Inicializacion del puerto Serie
}
 
void loop(){
  if(BT.available()){       // Si llega un dato por el puerto BT se envía al monitor serial
    Serial.write(BT.read());
  }
 
  if(Serial.available()){   // Si llega un dato por el monitor serial se envía al puerto BT
     BT.write(Serial.read());
  }
}
