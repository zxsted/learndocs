####Python subprocess shell编程规范
[原文传送门][0]
使用subprocess通过调用另一个模块组件时，需要对返回的code进行判断。判断失败时需要raise Exception，不然调用树过于复杂时，很难跟踪到异常发生的位置

sys.exit(1)虽然也可以达到对执行结果进行判断的目的，但是它难于追踪异常发生的位置。
实例：
调用树
```python
a.py
	`-- b.py
    	`-- ls
```
a.py
```python
import sys,subprocess

def exec_cmd(cmd):
	'''
    Run shell command
    '''
    p = subprocess.Popen(cmd,stdin = subprocess.PIPE,\
    	stdout = subprocess.PIPE,
        stderr = subprocess.STDOUT,
        shell  = True)
        
    log_content = p.communicate()[0]
    return p.returncode,log_content
    
def main():
	cmd = "python b.py"
    cmd_comd,cmd_log = exec_cmd(cmd)
    if not cmd_code == 0:
    	rasie Exception(cmd_log)   #抛出异常信息日志
        
if __name__ == '__main__':
	main()
```

b.py
```python
import sys,subprocess

def exec_cmd(cmd):
	"""
    Run shell command
    """
    p = subprocess.Popen(cmd,stdin=subprocess.PIPE,\
    	stdout = subprocess.PIPE,
        stderr = subprocess.STDOUT,
        shell  = True)
        
    log_content = p.communicate()[0]
    
    return p.returncode,log_content
    
def main():
	cmd = """ls c.py"""
    cmd_code,cmd_log = exec_cmd(cmd)
    if not cmd_code == 0:
    	raise Exception(cmd_log)
        
if __name__ == '__main__':
	main()
```














































[0]:http://blog.csdn.net/waterforest_pang/article/details/16885131