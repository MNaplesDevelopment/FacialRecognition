import json


class Entry:
    def __init__(self, image_path: str, facial_embedding: list, name: str, id: str, border: dict):
        self.image_path = image_path
        self.facial_embedding = facial_embedding
        self.name = name
        self.id = id
        self.border = border

    def to_json(self):
        return json.dumps(self.__dict__)

