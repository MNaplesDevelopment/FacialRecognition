import numpy as np
from numpy.linalg import norm
from deepface import DeepFace
import matplotlib.pyplot as plt
import cv2 as cv

from database_entry import Entry

threshold = 0.68


def cosine_similarity(vec1, vec2):
    return 1 - (np.dot(vec1, vec2) / (norm(vec1) * norm(vec2)))


def reformat_border(facial_area):
    top, left = facial_area['y'], facial_area['x']
    bottom, right = top + facial_area['w'], left + facial_area['h']
    border = {
        'top': top,
        'left': left,
        'bottom': bottom,
        'right': right
    }
    return border


def save_img_prediction(image_path: str, facial_border: dict, name: str):
    image = cv.cvtColor(cv.imread(image_path), cv.COLOR_BGR2RGB)

    top, left = facial_border['top'], facial_border['left']
    bottom, right = facial_border['bottom'], facial_border['right']

    cv.rectangle(image, (left, top), (right, bottom), (0, 255, 0), 3)

    font_scale = image.shape[1] / 300
    text_pos = (left, bottom + int(image.shape[0] * 0.06))
    cv.putText(image, name, text_pos, cv.FONT_HERSHEY_PLAIN, font_scale, (0, 255, 0), 3)
    cv.imwrite('identified-person.jpg', cv.cvtColor(image, cv.COLOR_RGB2BGR))

    #plt.figure(figsize=(10, 5))
    #plt.imshow(image)
    #plt.show()


class VectorDataBase:
    def __init__(self):
        self.db = {}

    def add_entry(self, image_path: str, name: str, id: int):
        try:
            face = DeepFace.represent(image_path)
        except ValueError:
            print(f'Face not detected at: {image_path}')
            return

        embedding = face[0]['embedding']
        facial_area = face[0]['facial_area']
        border = reformat_border(facial_area)

        entry = Entry(image_path, embedding, name, id, border)

        if name not in self.db.keys():
            self.db[name] = [entry]
        else:
            self.db[name].append(entry)


    def find_matches(self, image_path: str):
        face = DeepFace.represent(image_path)
        embedding = face[0]['embedding']
        matching_persons = []
        for name in self.db.keys():
            for entry in self.db[name]:
                if cosine_similarity(embedding, entry.facial_embedding) <= threshold:
                    matching_persons.append(name)
                    break
        return matching_persons, reformat_border(face[0]['facial_area'])




