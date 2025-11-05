import requests

def download_word_document():
    url = "http://localhost:8080/api/download"
    
    params = {
        'fileUrl': 'download/20251105/translate_4d29e986-ba34-434e-bd42-08d66937e6fa.docx'
    }
    
    headers = {
        'Accept': '*/*',
        'Accept-Encoding': 'gzip, deflate, br',
        'Connection': 'keep-alive',
        'Cookie': 'JSESSIONID=076504CC4F6AA0FBDB20D50372EE25B0',
        'User-Agent': 'PostmanRuntime-ApipostRuntime/1.1.0'
    }
    
    try:
        # 发送 GET 请求
        response = requests.get(url, params=params, headers=headers, stream=True)
        
        # 检查请求是否成功
        if response.status_code == 200:
            # 从 Content-Disposition 头获取文件名，如果没有则使用默认名
            content_disposition = response.headers.get('Content-Disposition')
            print(content_disposition)
            if content_disposition and 'filename=' in content_disposition:
                filename = content_disposition.split('filename=')[1].strip('"')
            else:
                filename = "translated_document.docx"
            
            # 保存文件
            with open(filename, 'wb') as file:
                for chunk in response.iter_content(chunk_size=8192):
                    file.write(chunk)
            
            print(f"文件下载成功: {filename}")
        else:
            print(f"下载失败，状态码: {response.status_code}")
            print(f"响应内容: {response.text}")
            
    except requests.exceptions.RequestException as e:
        print(f"请求异常: {e}")

if __name__ == "__main__":
    download_word_document()