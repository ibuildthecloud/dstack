import json
from cattle.utils import JsonObject


class Marshaller:
    def __init__(self):
        pass

    def from_string(self, string):
        obj = json.loads(string)
        return JsonObject(obj)

    def to_string(self, obj):
        return json.dumps(dict(obj))
