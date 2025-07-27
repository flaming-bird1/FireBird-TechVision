import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:5000',
    timeout: 10000
})

export const predictImage = async (file) => {
    const formData = new FormData()
    formData.append('image', file)
    try {
        const response = await api.post('/predict', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
        return response.data
    } catch (error) {
        console.error('Prediction error:', error)
        throw error
    }
}