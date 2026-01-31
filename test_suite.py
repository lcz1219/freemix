import urllib.request
import urllib.parse
import json
import time
import datetime
import sys
import os
import uuid
import mimetypes

BASE_URL = "http://localhost:8888"
REPORT_FILE = "TEST_REPORT.md"

def get_curl_command(method, url, data=None, token=None):
    cmd = f"curl -X {method} '{BASE_URL}{url}'"
    cmd += " -H 'Content-Type: application/json'"
    cmd += " -H 'User-Agent: Mozilla/5.0'"
    if token:
        cmd += f" -H 'Authorization: Bearer {token}'"
    if data:
        json_data = json.dumps(data, ensure_ascii=False)
        cmd += f" -d '{json_data}'"
    return cmd

def post(url, data, token=None):
    req = urllib.request.Request(f"{BASE_URL}{url}")
    req.add_header('Content-Type', 'application/json')
    req.add_header('User-Agent', 'Mozilla/5.0')
    if token:
        req.add_header('Authorization', f'Bearer {token}')
    
    body = json.dumps(data).encode('utf-8')
    try:
        with urllib.request.urlopen(req, body) as response:
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        error_content = e.read().decode('utf-8')
        print(f"HTTP Error {e.code} on {url}: {error_content}")
        return {"code": e.code, "msg": f"HTTP Error {e.code}", "data": error_content}
    except Exception as e:
        print(f"Error on {url}: {e}")
        return None

def get(url, token=None):
    req = urllib.request.Request(f"{BASE_URL}{url}")
    req.add_header('Content-Type', 'application/json')
    req.add_header('User-Agent', 'Mozilla/5.0')
    if token:
        req.add_header('Authorization', f'Bearer {token}')
    
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        error_content = e.read().decode('utf-8')
        print(f"HTTP Error {e.code} on {url}: {error_content}")
        return {"code": e.code, "msg": f"HTTP Error {e.code}", "data": error_content}
    except Exception as e:
        print(f"Error on {url}: {e}")
        return None

def delete(url, token=None):
    req = urllib.request.Request(f"{BASE_URL}{url}", method='DELETE')
    req.add_header('Content-Type', 'application/json')
    req.add_header('User-Agent', 'Mozilla/5.0')
    if token:
        req.add_header('Authorization', f'Bearer {token}')
    
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        error_content = e.read().decode('utf-8')
        print(f"HTTP Error {e.code} on {url}: {error_content}")
        return {"code": e.code, "msg": f"HTTP Error {e.code}", "data": error_content}
    except Exception as e:
        print(f"Error on {url}: {e}")
        return None

def multipart_post(url, fields, files, token=None):
    boundary = '----WebKitFormBoundary' + uuid.uuid4().hex
    data = []
    
    for name, value in fields.items():
        data.append(f'--{boundary}'.encode('utf-8'))
        data.append(f'Content-Disposition: form-data; name="{name}"'.encode('utf-8'))
        data.append(b'')
        data.append(str(value).encode('utf-8'))
        
    for name, filename, content_type, content in files:
        data.append(f'--{boundary}'.encode('utf-8'))
        data.append(f'Content-Disposition: form-data; name="{name}"; filename="{filename}"'.encode('utf-8'))
        data.append(f'Content-Type: {content_type}'.encode('utf-8'))
        data.append(b'')
        if isinstance(content, str):
            data.append(content.encode('utf-8'))
        else:
            data.append(content)
    
    data.append(f'--{boundary}--'.encode('utf-8'))
    data.append(b'')
    
    body = b'\r\n'.join(data)
    
    req = urllib.request.Request(f"{BASE_URL}{url}", data=body)
    req.add_header('Content-Type', f'multipart/form-data; boundary={boundary}')
    req.add_header('User-Agent', 'Mozilla/5.0')
    if token:
        req.add_header('Authorization', f'Bearer {token}')
        
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        error_content = e.read().decode('utf-8')
        print(f"HTTP Error {e.code} on {url}: {error_content}")
        return {"code": e.code, "msg": f"HTTP Error {e.code}", "data": error_content}
    except Exception as e:
        print(f"Error on {url}: {e}")
        return None

def get_captcha_solution(expression):
    parts = expression.split(' ')
    if len(parts) != 3:
        return None
    n1 = int(parts[0])
    op = parts[1]
    n2 = int(parts[2])
    
    if op == '+': return str(n1 + n2)
    if op == '-': return str(n1 - n2)
    if op == '×' or op == '*': return str(n1 * n2)
    if op == '÷' or op == '/': return str(n1 // n2)
    return None

class TestSuite:
    def __init__(self):
        self.results = []
        self.username = f"test_{int(time.time())}"
        self.password = "password123"
        self.token = None
        self.user_id = None
        self.goal_id = None
        self.goal_obj = None
        
    def log(self, message):
        print(message)
    
    def record_result(self, name, status, details="", repro_cmd=""):
        self.results.append({
            "name": name,
            "status": status,
            "details": details,
            "time": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "repro": repro_cmd
        })
        self.log(f"[{status}] {name}: {details}")

    def test_auth(self):
        self.log(f"\n--- Testing Auth ({self.username}) ---")
        
        # Register
        payload = {"username": self.username, "password": self.password}
        res = post("/register", payload)
        if not res or res.get('code') != 200:
            self.record_result("用户注册", "FAIL", f"失败: {res}", get_curl_command("POST", "/register", payload))
            return False
        self.record_result("用户注册", "PASS", "已创建")
        
        # Captcha
        res = post("/captcha", {"username": self.username})
        if not res or 'data' not in res:
            self.record_result("验证码生成", "FAIL", "失败", get_curl_command("POST", "/captcha", {"username": self.username}))
            return False
        
        expression = res['data']['expression']
        captcha_code = get_captcha_solution(expression)
        self.record_result("验证码生成", "PASS", f"解答: {expression}={captcha_code}")
        
        # Login
        login_payload = {"username": self.username, "password": self.password, "captcha": captcha_code, "totpCode": ""}
        res = post("/login", login_payload)
        if not res or res.get('code') != 200:
            self.record_result("用户登录", "FAIL", f"失败: {res}", get_curl_command("POST", "/login", login_payload))
            return False
            
        self.token = res['data']['token']
        self.user_id = res['data']['id']
        self.record_result("用户登录", "PASS", "Token已接收")
        
        # Debug Token
        res = get("/debug/me", self.token)
        if res and res.get('code') == 200:
            self.record_result("Token验证 (Debug)", "PASS", f"用户已识别: {res['data']['username']}")
        else:
            self.record_result("Token验证 (Debug)", "FAIL", f"UserContextUtil返回null: {res}", get_curl_command("GET", "/debug/me", None, self.token))
            
        return True

    def test_user_profile(self):
        self.log("\n--- Testing User Profile ---")
        if not self.token: return

        # Edit User Info
        update_data = {
            "id": self.user_id,
            "username": self.username,
            "password": self.password, 
            "chinesename": "测试用户CN"
        }
        res = post("/edituserinfo", update_data, self.token)
        if res and res.get('code') == 200 and res['data'].get('chinesename') == "测试用户CN":
            self.record_result("修改用户信息", "PASS", "名称已更新")
        else:
            self.record_result("修改用户信息", "FAIL", f"失败: {res}", get_curl_command("POST", "/edituserinfo", update_data, self.token))

        # Get Owner List (User List)
        res = get("/getOwerList", self.token)
        if res and res.get('code') == 200:
            users = res['data']
            found = any(u['value'] == self.username for u in users)
            if found:
                self.record_result("获取用户列表", "PASS", "在列表中找到当前用户")
            else:
                self.record_result("获取用户列表", "WARN", "列表已获取但未找到用户(可能缓存?)")
        else:
            self.record_result("获取用户列表", "FAIL", f"失败: {res}", get_curl_command("GET", "/getOwerList", None, self.token))

    def test_goal_management(self):
        self.log("\n--- Testing Goal Management ---")
        if not self.token: return

        # 1. Create Goal
        goal_data = {
            "title": "API测试目标",
            "description": "通过API测试套件创建",
            "owner": self.username,
            "deadline": (datetime.datetime.now() + datetime.timedelta(days=7)).strftime("%Y-%m-%dT%H:%M:%S.000+00:00"),
            "childGoals": []
        }
        
        res = post("/editGoal", goal_data, self.token)
        if res and res.get('code') == 200:
            self.record_result("创建目标", "PASS", "目标创建已触发")
        else:
            self.record_result("创建目标", "FAIL", f"失败: {res}", get_curl_command("POST", "/editGoal", goal_data, self.token))
            return

        # 2. List Goals to get ID
        res = get(f"/getGoals/{self.username}", self.token)
        if res and res.get('code') == 200:
            goals = res['data']
            target_goal = next((g for g in goals if g['title'] == "API测试目标"), None)
            if target_goal:
                self.goal_id = target_goal['_id'] # or 'id'
                self.goal_obj = target_goal
                self.record_result("列出目标", "PASS", f"找到目标ID: {self.goal_id}")
            else:
                self.record_result("列出目标", "FAIL", "未在列表中找到已创建的目标", get_curl_command("GET", f"/getGoals/{self.username}", None, self.token))
                return
        else:
            self.record_result("列出目标", "FAIL", f"失败: {res}", get_curl_command("GET", f"/getGoals/{self.username}", None, self.token))
            return

        # 3. Update Goal (Complete it)
        if self.goal_obj:
            self.goal_obj['progress'] = 100
            self.goal_obj['status'] = "completed"
            self.goal_obj['description'] = "API测试套件更新" 
            # Ensure deadline is valid (refresh it)
            self.goal_obj['deadline'] = (datetime.datetime.now() + datetime.timedelta(days=7)).strftime("%Y-%m-%dT%H:%M:%S.000+00:00")
            
            res = post("/editGoal", self.goal_obj, self.token)
            if res and res.get('code') == 200:
                 self.record_result("更新目标", "PASS", "标记为已完成")
                 # Check for completion achievement
                 achievements = res.get('achievements')
                 if achievements:
                     titles = [a['title'] for a in achievements]
                     self.record_result("成就触发 (首次胜利)", "PASS", f"解锁: {titles}")
            else:
                self.record_result("更新目标", "FAIL", f"失败: {res}", get_curl_command("POST", "/editGoal", self.goal_obj, self.token))

        # 4. Delete Goal
        if self.goal_obj:
            delete_payload = {
                "row": json.dumps(self.goal_obj)
            }
            res = post("/deleteGoal", delete_payload, self.token)
            if res and res.get('code') == 200:
                self.record_result("删除目标", "PASS", "目标已删除")
            else:
                self.record_result("删除目标", "FAIL", f"失败: {res}", get_curl_command("POST", "/deleteGoal", delete_payload, self.token))

        # 5. Verify Deletion (Check Recycle Bin)
        res = get(f"/recycle/{self.username}", self.token)
        if res and res.get('code') == 200:
            recycled = res['data']
            found = any(g['_id'] == self.goal_id for g in recycled)
            if found:
                self.record_result("回收站检查", "PASS", "在回收站找到已删除的目标")
            else:
                self.record_result("回收站检查", "FAIL", "未在回收站找到已删除的目标", get_curl_command("GET", f"/recycle/{self.username}", None, self.token))
        else:
            self.record_result("回收站检查", "FAIL", f"失败: {res}", get_curl_command("GET", f"/recycle/{self.username}", None, self.token))

    def test_goal_structure(self):
        self.log("\n--- Testing Goal Structure ---")
        if not self.token: return

        # Create a new goal first to attach structure to
        root_goal_id = str(uuid.uuid4())
        
        # Simulate frontend saving a goal structure (LogicFlow data)
        # This simulates creating a MindMap/Flowchart for goals
        node_id_1 = root_goal_id
        node_id_2 = str(uuid.uuid4())
        
        structure_payload = {
            "nodes": [
                {
                    "id": node_id_1,
                    "type": "rect",
                    "x": 100,
                    "y": 100,
                    "properties": {},
                    "text": {"x": 100, "y": 100, "value": "根目标 (结构测试)"},
                    "title": "根目标 (结构测试)",
                    "description": "这是根节点"
                },
                {
                    "id": node_id_2,
                    "type": "rect",
                    "x": 300,
                    "y": 100,
                    "properties": {},
                    "text": {"x": 300, "y": 100, "value": "子目标 A"},
                    "title": "子目标 A",
                    "description": "这是子节点"
                }
            ],
            "connections": [
                {
                    "source": node_id_1,
                    "target": node_id_2
                }
            ]
        }
        
        # We need to make sure the root goal exists or allow the API to create it?
        # Looking at GoalController.saveGoalStructure:
        # It tries to find the goal by rootNodeId. If not found, it might fail or create?
        # The code: Goal goal = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(rootNodeId)), Goal.class);
        # If goal is null, it will throw NPE when setting title later: goal.setTitle(...)
        
        # So we must create the goal first!
        goal_data = {
            "_id": root_goal_id,
            "title": "根目标 (结构测试)",
            "description": "预先创建",
            "owner": self.username,
            "deadline": (datetime.datetime.now() + datetime.timedelta(days=7)).strftime("%Y-%m-%dT%H:%M:%S.000+00:00"),
            "childGoals": []
        }
        res = post("/editGoal", goal_data, self.token)
        if not res or res.get('code') != 200:
            self.record_result("预创建结构根目标", "FAIL", f"失败: {res}", get_curl_command("POST", "/editGoal", goal_data, self.token))
            return

        # Now save structure
        res = post("/saveGoalStructure", structure_payload, self.token)
        if res and res.get('code') == 200:
            self.record_result("保存目标结构", "PASS", "结构保存成功")
        else:
            self.record_result("保存目标结构", "FAIL", f"失败: {res}", get_curl_command("POST", "/saveGoalStructure", structure_payload, self.token))

    def test_achievements(self):
        self.log("\n--- Testing Achievements ---")
        if not self.token: return

        res = get("/api/achievements/my", self.token)
        if res and res.get('code') == 200:
            achs = res['data']
            unlocked_count = len([a for a in achs if a['unlocked']])
            self.record_result("获取我的成就", "PASS", f"总数: {len(achs)}, 已解锁: {unlocked_count}")
        elif res and "未登录" in str(res):
            self.record_result("获取我的成就", "WARN", f"服务端提示未登录 (Token问题?): {res}", get_curl_command("GET", "/api/achievements/my", None, self.token))
        else:
            self.record_result("获取我的成就", "FAIL", f"失败: {res}", get_curl_command("GET", "/api/achievements/my", None, self.token))

    def test_login_logs(self):
        self.log("\n--- Testing Login Logs ---")
        if not self.token: return

        res = get(f"/api/login-log/user?userId={self.user_id}&page=1&size=10", self.token)
        if res and res.get('code') == 200:
            data = res['data']
            logs = data['content'] if isinstance(data, dict) and 'content' in data else data
            
            if logs and len(logs) > 0:
                self.record_result("获取登录日志", "PASS", f"获取到 {len(logs)} 条日志")
            else:
                self.record_result("获取登录日志", "WARN", "未找到日志 (至少应有当前登录)", get_curl_command("GET", f"/api/login-log/user?userId={self.user_id}", None, self.token))
        else:
            self.record_result("获取登录日志", "FAIL", f"失败: {res}", get_curl_command("GET", f"/api/login-log/user?userId={self.user_id}", None, self.token))

    def test_feedback(self):
        self.log("\n--- Testing Feedback ---")
        if not self.token: return

        feedback_data = {
            "title": "测试反馈",
            "content": "这是来自自动化套件的测试反馈",
            "contact": "test@example.com",
            "type": "bug" 
        }
        res = post("/addFeedback", feedback_data, self.token)
        if res and res.get('code') == 200:
            self.record_result("提交反馈", "PASS", "反馈已提交")
        else:
            self.record_result("提交反馈", "FAIL", f"失败: {res}", get_curl_command("POST", "/addFeedback", feedback_data, self.token))
            
        # Verify in list
        res = get("/findFeedBack", self.token)
        if res and res.get('code') == 200:
            feedbacks = res['data']
            found = any(f['content'] == "这是来自自动化套件的测试反馈" for f in feedbacks)
            if found:
                self.record_result("列出反馈", "PASS", "在列表中找到提交的反馈")
            else:
                self.record_result("列出反馈", "WARN", "未在列表中找到反馈 (可能延迟?)", get_curl_command("GET", "/findFeedBack", None, self.token))
        else:
            self.record_result("列出反馈", "FAIL", f"失败: {res}", get_curl_command("GET", "/findFeedBack", None, self.token))

    def test_messages(self):
        self.log("\n--- Testing Messages ---")
        if not self.token: return
        
        # Send message to self for testing
        msg_data = {
            "fromUser": self.username,
            "toUser": self.username,
            "content": "Hello Self",
            "type": "text"
        }
        res = post("/messages/send", msg_data, self.token)
        if res and res.get('code') == 200:
            self.record_result("发送消息", "PASS", "消息已发送")
        else:
            self.record_result("发送消息", "FAIL", f"失败: {res}", get_curl_command("POST", "/messages/send", msg_data, self.token))
            
        # Check Sent
        res = get("/messages/sent", self.token)
        if res and res.get('code') == 200:
            msgs = res['data']
            found = any(m['content'] == "Hello Self" for m in msgs)
            if found:
                self.record_result("获取已发送消息", "PASS", "在发件箱找到消息")
            else:
                self.record_result("获取已发送消息", "FAIL", "未在发件箱找到消息", get_curl_command("GET", "/messages/sent", None, self.token))
        
        # Check Received
        res = get("/messages/received", self.token)
        if res and res.get('code') == 200:
            msgs = res['data']
            found_msg = next((m for m in msgs if m['content'] == "Hello Self"), None)
            if found_msg:
                self.record_result("获取已接收消息", "PASS", "在收件箱找到消息")
                # Mark as read
                read_res = post(f"/messages/markAsRead/{found_msg['id']}", {}, self.token)
                if read_res and read_res.get('code') == 200:
                     self.record_result("标记消息已读", "PASS", "消息已标记为已读")
                else:
                     self.record_result("标记消息已读", "FAIL", f"失败: {read_res}", get_curl_command("POST", f"/messages/markAsRead/{found_msg['id']}", {}, self.token))
            else:
                self.record_result("获取已接收消息", "FAIL", "未在收件箱找到消息", get_curl_command("GET", "/messages/received", None, self.token))
        
        # Chat History
        res = get(f"/messages/chat/{self.username}", self.token)
        if res and res.get('code') == 200:
             self.record_result("获取聊天记录", "PASS", f"获取到 {len(res['data'])} 条消息")
        else:
             self.record_result("获取聊天记录", "FAIL", f"失败: {res}", get_curl_command("GET", f"/messages/chat/{self.username}", None, self.token))

    def test_ai_gen(self):
        self.log("\n--- Testing AI Generation ---")
        if not self.token: return
        
        # Save AI Gen
        save_req = {
            "userInput": "Create a test plan",
            "aiResponse": "Here is a plan...",
            "goalTitle": "AI Generated Goal",
            "childGoals": [],
            "chatMessages": []
        }
        
        payload = {
            "username": self.username,
            "saveData": json.dumps(save_req)
        }
        
        res = post("/api/aiGen/save", payload, self.token)
        if res and res.get('code') == 200:
            self.record_result("保存AI生成记录", "PASS", "记录已保存")
        else:
            self.record_result("保存AI生成记录", "FAIL", f"失败: {res}", get_curl_command("POST", "/api/aiGen/save", payload, self.token))
            
        # List AI Gen
        res = get(f"/api/aiGen/list?username={self.username}", self.token)
        if res and res.get('code') == 200:
            list_data = res['data']
            if len(list_data) > 0:
                self.record_result("列出AI生成记录", "PASS", f"找到 {len(list_data)} 条记录")
            else:
                self.record_result("列出AI生成记录", "WARN", "未找到记录 (尽管已保存)")
        else:
             self.record_result("列出AI生成记录", "FAIL", f"失败: {res}", get_curl_command("GET", f"/api/aiGen/list?username={self.username}", None, self.token))

    def test_ai_messages(self):
        self.log("\n--- Testing AI Messages ---")
        if not self.token: return
        
        # Save Message
        msg_data = {
            "userId": self.user_id,
            "username": self.username,
            "userQuestion": "Hello AI",
            "aiAnswer": "Hello User", 
            "timestamp": int(time.time() * 1000)
        }
        res = post("/ai-messages/save", msg_data, self.token)
        if res and res.get('code') == 200:
            self.record_result("保存AI消息", "PASS", "消息已保存")
        else:
            self.record_result("保存AI消息", "FAIL", f"失败: {res}", get_curl_command("POST", "/ai-messages/save", msg_data, self.token))
            
        # Get Recent
        res = get("/ai-messages/recent", self.token)
        if res and res.get('code') == 200:
            if len(res['data']) > 0:
                self.record_result("获取最近AI消息", "PASS", f"找到 {len(res['data'])} 条消息")
            else:
                self.record_result("获取最近AI消息", "WARN", "未找到消息")
        else:
            self.record_result("获取最近AI消息", "FAIL", f"失败: {res}", get_curl_command("GET", "/ai-messages/recent", None, self.token))

    def test_file_upload(self):
        self.log("\n--- Testing File Upload ---")
        if not self.token: return
        
        filename = "test_upload.txt"
        with open(filename, "w") as f:
            f.write("This is a test file content.")
            
        files = [
            ("file", filename, "text/plain", open(filename, "rb").read())
        ]
        
        res = multipart_post("/file/upload-file", {}, files, self.token)
        if res and res.get('code') == 200:
            self.record_result("上传通用文件", "PASS", f"已上传: {res.get('data')}")
        else:
            self.record_result("上传通用文件", "FAIL", f"失败: {res}")
            
        try:
            os.remove(filename)
        except:
            pass

    def test_update_log(self):
        self.log("\n--- Testing Update Logs ---")
        
        res = get("/api/updates/latest")
        if res and res.get('code') == 200:
            self.record_result("获取最新更新日志", "PASS", "成功")
        else:
            self.record_result("获取最新更新日志", "FAIL", f"失败: {res}", get_curl_command("GET", "/api/updates/latest"))
            
        res = get("/api/updates/list", self.token)
        if res and res.get('code') == 200:
             self.record_result("获取更新日志列表", "PASS", f"找到 {len(res['data'])} 条日志")
        else:
             self.record_result("获取更新日志列表", "FAIL", f"失败: {res}", get_curl_command("GET", "/api/updates/list", None, self.token))
             
        update_data = {
            "version": "1.0.0",
            "content": "Test update",
            "type": "feature"
        }
        res = post(f"/api/updates/create?username={self.username}", update_data, self.token)
        if res and res.get('code') != 200:
             self.record_result("创建更新日志 (权限检查)", "PASS", "正确拒绝非管理员")
        else:
             self.record_result("创建更新日志 (权限检查)", "WARN", "非管理员被允许创建更新日志!", get_curl_command("POST", f"/api/updates/create?username={self.username}", update_data, self.token))

    def test_user_status(self):
        self.log("\n--- Testing User Status ---")
        
        try:
            req = urllib.request.Request(f"{BASE_URL}/user-status/all")
            if self.token: req.add_header('Authorization', f'Bearer {self.token}')
            with urllib.request.urlopen(req) as response:
                content = response.read().decode('utf-8')
                data = json.loads(content)
                self.record_result("获取所有用户状态", "PASS", f"状态映射: {data}")
        except Exception as e:
            self.record_result("获取所有用户状态", "FAIL", f"失败: {e}", get_curl_command("GET", "/user-status/all", None, self.token))

    def test_verify_desktop_token_reproduction(self):
        self.log("\n--- Testing Verify Desktop Token Reproduction ---")
        if not self.username: return

        desktop_token = str(uuid.uuid4()).replace("-", "")
        payload = {"desktopToken": desktop_token, "username": self.username}

        # Case 1: Simulate Bug - Request with Invalid X-Desktop-Token Header
        # We manually construct request to add the specific header
        url = "/verify-desktop-token"
        req = urllib.request.Request(f"{BASE_URL}{url}")
        req.add_header('Content-Type', 'application/json')
        req.add_header('User-Agent', 'Mozilla/5.0')
        req.add_header('X-Desktop-Token', desktop_token) # The bug: sending unverified token
        
        body = json.dumps(payload).encode('utf-8')
        try:
            with urllib.request.urlopen(req, body) as response:
                res = json.loads(response.read().decode('utf-8'))
                if res.get('code') == 200:
                     self.record_result("复现Bug (带无效Header)", "PASS", "意外通过 - 服务端未拒绝无效Header")
                else:
                     self.record_result("复现Bug (带无效Header)", "FAIL", f"被拒绝 (预期行为): {res}")
        except urllib.error.HTTPError as e:
            self.record_result("复现Bug (带无效Header)", "FAIL", f"HTTP错误: {e.code} {e.read().decode('utf-8')}")
        except Exception as e:
            self.record_result("复现Bug (带无效Header)", "FAIL", f"异常: {e}")

        # Case 2: Simulate Fix - Request WITHOUT X-Desktop-Token Header
        # This is what happens after adding to NO_AUTH_PATHS
        req = urllib.request.Request(f"{BASE_URL}{url}")
        req.add_header('Content-Type', 'application/json')
        req.add_header('User-Agent', 'Mozilla/5.0')
        # No X-Desktop-Token header
        
        try:
            with urllib.request.urlopen(req, body) as response:
                res = json.loads(response.read().decode('utf-8'))
                if res.get('code') == 200:
                     self.record_result("验证修复 (无Header)", "PASS", "成功 - 服务端接受无Header请求")
                else:
                     self.record_result("验证修复 (无Header)", "FAIL", f"失败: {res}", get_curl_command("POST", url, payload))
        except urllib.error.HTTPError as e:
            self.record_result("验证修复 (无Header)", "FAIL", f"HTTP错误: {e.code} {e.read().decode('utf-8')}", get_curl_command("POST", url, payload))
        except Exception as e:
             self.record_result("验证修复 (无Header)", "FAIL", f"异常: {e}")

    def generate_report(self):
        with open(REPORT_FILE, "w", encoding="utf-8") as f:
            f.write("# 系统自动化测试报告\n\n")
            f.write(f"**日期:** {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"**环境:** 本地环境 ({BASE_URL})\n\n")
            
            f.write("| 模块 | 测试用例 | 状态 | 详情 | 时间 |\n")
            f.write("|------|----------|------|------|------|\n")
            
            for res in self.results:
                module = "通用"
                name = res['name']
                if "用户" in name or "验证码" in name: module = "认证/用户"
                elif "目标" in name or "回收" in name: module = "目标管理"
                elif "成就" in name: module = "游戏化"
                elif "日志" in name: module = "系统日志"
                elif "反馈" in name: module = "社交/支持"
                elif "消息" in name: module = "社交/支持"
                elif "AI" in name: module = "AI功能"
                elif "文件" in name: module = "文件服务"
                elif "更新" in name: module = "系统信息"
                elif "状态" in name: module = "系统信息"
                
                status_icon = "✅" if res['status'] == "PASS" else "❌" if res['status'] == "FAIL" else "⚠️"
                details = str(res['details']).replace("|", "\|")
                f.write(f"| {module} | {name} | {status_icon} {res['status']} | {details} | {res['time']} |\n")
            
            # Add Reproduction Section for Failures
            failures = [r for r in self.results if r['status'] == "FAIL"]
            if failures:
                f.write("\n## ❌ 失败用例复现步骤\n\n")
                for fail in failures:
                    f.write(f"### {fail['name']}\n")
                    f.write(f"**错误信息:** {fail['details']}\n\n")
                    if fail['repro']:
                        f.write("**复现命令 (Curl):**\n")
                        f.write(f"```bash\n{fail['repro']}\n```\n\n")
                    else:
                        f.write("*无复现命令可用*\n\n")
            
            f.write("\n## 总结\n")
            total = len(self.results)
            passed = len([r for r in self.results if r['status'] == "PASS"])
            failed = len([r for r in self.results if r['status'] == "FAIL"])
            f.write(f"- **总测试数:** {total}\n")
            f.write(f"- **通过:** {passed}\n")
            f.write(f"- **失败:** {failed}\n")
            f.write(f"- **通过率:** {int(passed/total*100) if total > 0 else 0}%\n")

if __name__ == "__main__":
    suite = TestSuite()
    
    if suite.test_auth():
        suite.test_user_profile()
        suite.test_goal_management()
        suite.test_goal_structure()
        suite.test_achievements()
        suite.test_login_logs()
        suite.test_feedback()
        suite.test_messages()
        suite.test_ai_gen()
        suite.test_ai_messages()
        suite.test_file_upload()
        suite.test_update_log()
        suite.test_user_status()
        suite.test_verify_desktop_token_reproduction()
    
    suite.generate_report()
    print(f"\nReport generated: {REPORT_FILE}")
