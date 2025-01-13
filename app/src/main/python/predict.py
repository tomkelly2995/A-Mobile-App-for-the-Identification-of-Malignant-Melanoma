import tensorflow as tf
import numpy as np


def predict(imageUrl):
    new_model = tf.keras.models.load_model('src/main/assets/model.tflite')

    oldimage  = Image.open(imageFileName)
    image = oldimage.resize((200, 150))
    
    myImage = np.asarray(image).astype('float32')
    
    
    x=tf.reshape(myImage,(1,150, 200,3))/255.0

    return (new_model.predict(x))