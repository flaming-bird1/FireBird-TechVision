import torch
import torchvision.transforms as transforms
from PIL import Image
import io
from app.models import CIFAR10Model

class CIFAR10Predictor:
    def __init__(self, model_path):
        self.model = self._load_model(model_path)
        self.class_list = ["飞机", "汽车", "鸟", "猫", "鹿", "狗", "青蛙", "马", "船", "卡车"]
        self.transform = transforms.Compose([
            transforms.Resize((32, 32)),
            transforms.ToTensor()
        ])

    def _load_model(self, path):
        model = CIFAR10Model()
        model.load_state_dict(torch.load(path, map_location='cpu', weights_only=False))
        model.eval()
        return model

    def predict(self, image_bytes):
        image = Image.open(io.BytesIO(image_bytes)).convert('RGB')
        tensor = self.transform(image).unsqueeze(0)
        with torch.no_grad():
            output = self.model(tensor)
            probabilities = torch.softmax(output, dim=1)[0]
            pred_class = torch.argmax(output).item()
        return {
            'prediction': self.class_list[pred_class],
            'confidence': float(probabilities[pred_class].item()),
            'probabilities': {
                cls: float(prob)
                for cls, prob in zip(self.class_list, probabilities)
            }
        }