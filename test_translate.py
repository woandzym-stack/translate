import requests
import json

def translate_text():
    url = "http://localhost:8080/api/v1/translate"
    
    headers = {
        'Accept': '*/*',
        'Accept-Encoding': 'gzip, deflate, br',
        'Connection': 'keep-alive',
        'Content-Type': 'application/json',
        'Cookie': 'JSESSIONID=076504CC4F6AA0FBDB20D50372EE25B0',
        'User-Agent': 'PostmanRuntime-ApipostRuntime/1.1.0'
    }
    
    payload = {
        "text": "Machine learning is a subset of artificial intelligence that enables computers to learn without being explicitly programmed.",
        "output_format": "word", 
        "include_vocabulary": True
    }
    
    try:
        response = requests.post(url, headers=headers, json=payload, timeout=30)
        response.raise_for_status()  # 如果状态码不是200，抛出异常
        
        result = response.json()
        print("翻译结果:", result)
        return result
        
    except requests.exceptions.Timeout:
        print("请求超时")
    except requests.exceptions.ConnectionError:
        print("连接错误，请检查URL和网络")
    except requests.exceptions.HTTPError as e:
        print(f"HTTP错误: {e}")
    except json.JSONDecodeError:
        print("响应不是有效的JSON格式")
    except Exception as e:
        print(f"其他错误: {e}")

if __name__ == "__main__":
    translate_text()