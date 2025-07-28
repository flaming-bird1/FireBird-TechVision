import torch
import torchvision.transforms as transforms
from PIL import Image
import io
from app.models.models import MNISTModel


class MNISTPredictor:
    def __init__(self, model_path):
        self.model = self._load_model(model_path)
        self.transform = transforms.Compose([
            transforms.Resize((28, 28)),
            transforms.ToTensor(),
            transforms.Lambda(lambda x: 1 - x if x.mean() > 0.5 else x)
        ])

    def _load_model(self, path):
        model = MNISTModel()
        model.load_state_dict(torch.load(path, map_location='cpu', weights_only=False))
        model.eval()
        return model

    def predict(self, image_bytes):
        img = Image.open(io.BytesIO(image_bytes)).convert('L')
        tensor = self.transform(img).unsqueeze(0)
        with torch.no_grad():
            outputs = self.model(tensor)
            probs = torch.softmax(outputs, dim=1)[0]
            pred = torch.argmax(probs).item()
        return {
            'prediction': int(pred),
            'probabilities': [round(p.item(), 4) for p in probs]
        }
