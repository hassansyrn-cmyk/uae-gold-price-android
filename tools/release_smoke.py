#!/usr/bin/env python3
"""Smoke-test the actual minified signed release APK on a CI emulator."""
import json
import pathlib
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PKG = 'com.uae.goldprice'
OUT = pathlib.Path('smoke-results')
OUT.mkdir(exist_ok=True)
results = []

def adb(*args, check=True):
    p = subprocess.run(['adb', *args], capture_output=True, check=check)
    return p.stdout.decode(errors='replace')

def shell(*args, check=True):
    return adb('shell', *args, check=check)

def ui(label):
    # Compose animations can prevent UIAutomator reaching idle; disable its wait.
    for _ in range(4):
        shell('uiautomator', 'dump', '/sdcard/release-ui.xml', check=False)
        text = shell('cat', '/sdcard/release-ui.xml', check=False)
        if '<hierarchy' in text:
            (OUT / (label + '.xml')).write_text(text)
            return ET.fromstring(text[text.index('<?xml'):] if '<?xml' in text else text)
        time.sleep(2)
    raise AssertionError('Could not inspect release UI: ' + label)

def screenshot(label):
    p = subprocess.run(['adb', 'exec-out', 'screencap', '-p'], capture_output=True, check=True)
    (OUT / (label + '.png')).write_bytes(p.stdout)

def running(label):
    assert shell('pidof', PKG).strip(), 'Release process exited: ' + label
    window = shell('dumpsys', 'activity', 'activities')
    assert PKG in window, 'App activity missing: ' + label
    screenshot(label)
    results.append(label + ': PASS')
    print(results[-1], flush=True)

def tap_text(root, text):
    found = [n for n in root.iter('node') if n.get('text') == text or n.get('content-desc') == text]
    assert len(found) == 1, 'Expected one control: ' + text
    bounds = [int(x) for x in re.findall(r'\d+', found[0].get('bounds', ''))]
    assert len(bounds) == 4
    shell('input', 'tap', str((bounds[0] + bounds[2]) // 2), str((bounds[1] + bounds[3]) // 2))
    time.sleep(4)

def launch():
    shell('am', 'start', '-W', '-n', PKG + '/.MainActivity')
    time.sleep(10)

try:
    adb('install', '-r', sys.argv[1])
    info = shell('dumpsys', 'package', PKG)
    assert 'versionCode=7' in info and 'versionName=1.3.2' in info and 'targetSdk=36' in info
    (OUT / 'package.txt').write_text(info)
    shell('pm', 'grant', PKG, 'android.permission.POST_NOTIFICATIONS')
    adb('logcat', '-c')
    launch()
    running('arabic-portrait')
    root = ui('arabic-portrait')
    tap_text(root, 'English')
    running('english-portrait')
    root = ui('english-portrait')
    assert any(n.get('text') == 'UAE Gold Market' for n in root.iter('node'))
    tap_text(root, 'AED')
    root = ui('usd')
    assert any(n.get('text') == 'USD' for n in root.iter('node'))
    results.append('currency-toggle: PASS')
    # Default system back must finish the single-activity app and permit relaunch.
    shell('input', 'keyevent', '4')
    time.sleep(2)
    launch()
    running('back-relaunch')
    shell('settings', 'put', 'system', 'accelerometer_rotation', '0')
    shell('settings', 'put', 'system', 'user_rotation', '1')
    time.sleep(5)
    running('english-landscape')
    shell('settings', 'put', 'system', 'user_rotation', '0')
    shell('cmd', 'overlay', 'enable-exclusive', '--category', 'com.android.internal.systemui.navbar.threebutton', check=False)
    time.sleep(4)
    launch()
    running('three-button-navigation')
    # Verify permission denial does not prevent startup.
    shell('am', 'force-stop', PKG)
    shell('pm', 'revoke', PKG, 'android.permission.POST_NOTIFICATIONS')
    launch()
    shell('pm', 'grant', PKG, 'android.permission.POST_NOTIFICATIONS')
    shell('input', 'keyevent', '4')
    launch()
    running('permission-recovery')
    log = adb('logcat', '-d')
    (OUT / 'logcat.txt').write_text(log)
    assert not re.search(r'FATAL EXCEPTION[^\n]*\n[^\n]*Process: com\.uae\.goldprice', log), 'App crash in logcat'
finally:
    (OUT / 'results.json').write_text(json.dumps(results, indent=2))
    (OUT / 'logcat-final.txt').write_text(adb('logcat', '-d', check=False))
