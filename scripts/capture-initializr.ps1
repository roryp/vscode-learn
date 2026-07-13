<#
    capture-initializr.ps1

    Captures the Spring Initializr QuickPick (the "Specify Spring Boot version"
    picker) for docs/images/00-initializr.png. That picker is a transient VS Code
    QuickPick, so this script drives it with keystrokes:
      1. Foreground the VS Code window.
      2. Open the Command Palette with F1 (single key - chorded Ctrl+Shift+P is
         unreliable via SendKeys) and run "Spring Initializr: Create a Maven Project".
      3. Wait for the Spring Boot version list to load from start.spring.io.
      4. Capture the window (PrintWindow + PW_RENDERFULLCONTENT).
      5. Press Esc repeatedly to cancel the wizard - NO project is generated.

    Requires the Spring Initializr extension (vscjava.vscode-spring-initializr).

    Examples (run from the repo root):
        pwsh -NoProfile -File scripts/capture-initializr.ps1 -Out docs/images/00-initializr.png
        pwsh -NoProfile -File scripts/capture-initializr.ps1 -Out docs/images/00-initializr.png -LoadMs 7000
#>
param(
    [string]$Out = "$env:TEMP\initcap.png",
    [int]$LoadMs = 5000
)

Add-Type -AssemblyName System.Windows.Forms, System.Drawing
if (-not ('WinCap' -as [type])) {
    Add-Type @"
using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Text;
public class WinCap {
    public delegate bool EnumProc(IntPtr hWnd, IntPtr lParam);
    [DllImport("user32.dll")] static extern bool EnumWindows(EnumProc cb, IntPtr p);
    [DllImport("user32.dll")] public static extern bool IsWindowVisible(IntPtr h);
    [DllImport("user32.dll",CharSet=CharSet.Unicode)] public static extern int GetWindowText(IntPtr h, StringBuilder s, int n);
    [DllImport("user32.dll")] public static extern int GetWindowTextLength(IntPtr h);
    [DllImport("user32.dll")] public static extern uint GetWindowThreadProcessId(IntPtr h, out uint pid);
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h, out RECT r);
    [DllImport("user32.dll")] public static extern bool PrintWindow(IntPtr h, IntPtr hdc, uint flags);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr h, int cmd);
    [DllImport("user32.dll")] public static extern bool SetProcessDPIAware();
    public struct RECT { public int Left, Top, Right, Bottom; }
    static List<IntPtr> _l; static EnumProc _cb;
    static bool A(IntPtr h, IntPtr p){ _l.Add(h); return true; }
    public static IntPtr[] W(){ _l=new List<IntPtr>(); _cb=new EnumProc(A); EnumWindows(_cb, IntPtr.Zero); return _l.ToArray(); }
    public static string T(IntPtr h){ int n=GetWindowTextLength(h); if(n<=0) return ""; var sb=new StringBuilder(n+1); GetWindowText(h,sb,sb.Capacity); return sb.ToString(); }
    public static uint P(IntPtr h){ uint p; GetWindowThreadProcessId(h, out p); return p; }
}
"@
}
[void][WinCap]::SetProcessDPIAware()

# Find the VS Code main window (title ends in 'Visual Studio Code').
$pids = @((Get-Process -Name Code -ErrorAction SilentlyContinue).Id)
$hwnd = [IntPtr]::Zero
foreach ($h in [WinCap]::W()) {
    if (-not [WinCap]::IsWindowVisible($h)) { continue }
    if ($pids -notcontains [int][WinCap]::P($h)) { continue }
    $t = [WinCap]::T($h); if (-not $t) { continue }
    if ($t -like '*Visual Studio Code*') { $hwnd = $h; break }
}
if ($hwnd -eq [IntPtr]::Zero) { throw "VS Code window not found" }

$wpid = [int][WinCap]::P($hwnd)
[void][WinCap]::ShowWindow($hwnd, 9)          # SW_RESTORE
[void][WinCap]::SetForegroundWindow($hwnd)
$ws = New-Object -ComObject WScript.Shell
try { [void]$ws.AppActivate($wpid) } catch {}
Start-Sleep -Milliseconds 700

# Open the Command Palette with F1 (single key = reliable; it opens already in '>'
# command mode, so we type the command name WITHOUT a leading '>'). Chorded
# shortcuts like Ctrl+Shift+P / Ctrl+P are unreliable via SendKeys (drop/leak keys).
[void]$ws.SendKeys('{ESC}')
Start-Sleep -Milliseconds 250
[void]$ws.SendKeys('{F1}')
Start-Sleep -Milliseconds 900
[void]$ws.SendKeys('Spring Initializr: Create a Maven Project')
Start-Sleep -Milliseconds 900
[void]$ws.SendKeys('{ENTER}')

# Wait for the Spring Boot version list to load (network fetch from start.spring.io).
Start-Sleep -Milliseconds $LoadMs

# Capture the window (QuickPick overlay is part of the window render).
$r = New-Object WinCap+RECT
[void][WinCap]::GetWindowRect($hwnd, [ref]$r)
$w = $r.Right - $r.Left; $h = $r.Bottom - $r.Top
$bmp = New-Object System.Drawing.Bitmap($w, $h)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$hdc = $g.GetHdc()
[void][WinCap]::PrintWindow($hwnd, $hdc, 2)    # PW_RENDERFULLCONTENT
$g.ReleaseHdc($hdc); $g.Dispose()
$bmp.Save($Out, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
Write-Host ("Saved {0}  ({1} x {2})" -f $Out, $w, $h)

# Cancel the wizard so no project is generated (Esc backs out of each step).
for ($i = 0; $i -lt 8; $i++) { [void]$ws.SendKeys('{ESC}'); Start-Sleep -Milliseconds 150 }
Write-Host "Wizard cancelled"
