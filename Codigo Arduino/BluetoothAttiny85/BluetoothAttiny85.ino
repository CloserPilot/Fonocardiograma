#include <SoftwareSerial.h>   // Incluimos la librerÃ­a  SoftwareSerial  
#define pinTX 3
#define pinRX 4
#define  PIN_VOUT A1
SoftwareSerial BT(pinRX,pinTX);    // Definimos los pines RX y TX del Arduino conectados al Bluetooth


// Lee del puerto serial 'a' para iniciar el envio de datos y 'b' para detenerlo
#define  CASO_INICIO   'a'
#define  CASO_DETENER  'b'

int leido = 0;
bool envia_datos = false;

void setup() {
  BT.begin(38400);       
  analogReference(EXTERNAL);
}

// Envia el dato de forma binaria 
// 'a'-> CASO INICIO
// 'b'-> CASO DETENER


void loop() {  
  if(BT.available()){
    leido = BT.read();
    
    if(leido == CASO_INICIO)
      envia_datos = true;
    
    if(leido == CASO_DETENER)
      envia_datos = false;
  }
 
  if(envia_datos){
      i = analogRead(PIN_VOUT);
      BT.write(highByte(i));
      BT.write(lowByte(i));
  }
}
