import json

from flask import Flask, request
from vector_database import VectorDataBase, save_img_prediction
import os

app = Flask(__name__)
vdb = VectorDataBase()


def populate_db_with_test_data():
    file_paths = []
    for root, dirs, files in os.walk('./Faces'):
        for name in files:
            file_paths.append(os.path.join(root, name))

    for file in file_paths:
        current_name = file.split('_')[0]
        if current_name in vdb.db.keys():
            if len(vdb.db[current_name]) >= 5:
                continue
        vdb.add_entry(file, current_name, 0)


populate_db_with_test_data()


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/encode-image/', methods=['POST'])
def encode_image_from_server():
    if request.method == 'POST':
        decoded_data = request.data.decode('utf-8')
        params = json.loads(decoded_data)
        print(params)
        images = params['images']
        name = params['name']
        employee_id = params['employeeID']
        for image in images:
            vdb.add_entry(image, name, employee_id)
        return "added", 200


@app.route('/identify-person/', methods=['POST'])
def identify_person_from_server():
    if request.method == 'POST':
        decoded_data = request.data.decode('utf-8')
        params = json.loads(decoded_data)
        print(params)
        image = params['image']
        names, border = vdb.find_matches(image)
        save_img_prediction(image, border, names[0])
        return '{"photoPath": "' + os.getcwd() + '\\test.jpg"}', 200


if __name__ == '__main__':
    app.run(debug=False, port=5000)
