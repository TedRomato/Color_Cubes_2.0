#include<Adafruit_NeoPixel.h>
#include <AnalogReader.h>


Adafruit_NeoPixel pixels = new Adafruit_NeoPixel(4, 6, NEO_RGB + NEO_KHZ800);
AnalogReader analogReader(10,7,0,70,1000);
AnalogReader analogReader2(10,7,1,70,1000);

uint8_t ledsInNode = 2;
uint8_t nodes = 2;

uint16_t analogVal;
void setup() {
  Serial.begin(9600);
  pixels.begin();

}

void loop() {
  analogReader.update();
  analogReader2.update();
  setColorToNode(analogReader2.getCurrentValueMapped(0,nodes),valueToColor(analogReader.getCurrentValueMapped(0,62)), ledsInNode);
}



void setColorToNode(uint8_t node, uint32_t color, uint8_t ledsInNode) {
  for(uint8_t i = 0; i < ledsInNode; i++) {
    pixels.setPixelColor(node*ledsInNode + i, color);
  }
  pixels.show();
}


uint32_t valueToColor(uint8_t entryValue) {
  // layer definition 
  uint8_t layerCount = 4;
  // amount of possible colors in each layer of color wheel
  uint8_t layersColorCounts[layerCount] = {36, 24, 1};
  // base value added to all colors (different for each layer)
  uint8_t layerBase[layerCount] = {0, 24, 85};

  // count possible colors in all layers
  uint8_t possibleColors = 0;
  for(uint8_t i = 0; i < layerCount; ++i){
    possibleColors += layersColorCounts[i];
  }

  // holds index of a layer in which entryValue exists
  uint8_t valueLayer;
  // holds index of a color in it's layer
  uint8_t positionInLayer;


  uint16_t accumulator = entryValue;
  for(uint8_t i = 0; i < layerCount; ++i){
    // when accumulator - layer color count is smaller than zero
    // layer in which entry value lies, and it's layer index are found 

    if(accumulator < layersColorCounts[i]){
      valueLayer = i;
      positionInLayer = accumulator;
      break;
    }
    // otherwise subtract from accumulator and repeat 
    else{
      accumulator -= layersColorCounts[i];
    }
  }


  // maximal addition to color from base, that doesn't exceed 255
  uint8_t maxFromBase = 255 - layerBase[valueLayer]*3;
  // colors, that can be crated with base value of layer
  uint16_t layerPossibleColorsAmount = maxFromBase*3;
 
  // difference in value between each accesible value
  double colorDifference = layerPossibleColorsAmount/layersColorCounts[valueLayer];

  
  
  // determines which colores are being altered
  uint8_t currentColor = positionInLayer*colorDifference / maxFromBase; // 0(0 - maxFromBase) = red, 1(maxFromBase - 2*maxFromBase) = green, 2(2*maxFromBase - 3*maxFromBase) = blue
  // color to which is moved (clockwise)
  uint8_t mainColorVal =  (uint16_t)(positionInLayer*colorDifference) % maxFromBase;
  // color from which is moved (clockwise)
  uint8_t sideColorVal = maxFromBase - mainColorVal;



  // set values to appropriate color
  uint32_t color = 0;
  if(currentColor == 0) {
    color = ((color + mainColorVal) << 16) + sideColorVal;
  } else if(currentColor == 1) {
    color = (((color + sideColorVal) << 8) + mainColorVal) << 8;
  } else {
    color = ((color + sideColorVal) << 8) + mainColorVal;
  }
  // add base to color
  uint32_t base = layerBase[valueLayer];
  base = (base << 16) + (base << 8) + base;
  
  return color + base;
}
