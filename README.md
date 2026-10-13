# 點唱收音機 v1.0.0

作者:ArchieKUO

Android App(Jetpack Compose),目前為「介面階段」:10 個畫面、範例資料、假播放。深色 / 淺色跟隨系統。

## 取得 APK(跟 AI 來電同樣的方式)

1. 在 GitHub 建立新的 repo(例如 `jukebox`)。
2. 把這個資料夾裡的全部內容(包含隱藏資料夾 `.github`)上傳到 repo 根目錄。
3. 到 repo 的 Actions 分頁,等 "Build APK" 跑完(約 5~8 分鐘),或按 "Run workflow"。
4. 在該次執行頁面最下方的 Artifacts 下載 `jukebox-apk`,解壓縮得到 `jukebox-v1.0.0.apk`,安裝到手機。

## 畫面

啟動、首頁、分類、頻道列表、頻道詳情、播放中、搜尋、我的收藏、定時關閉、設定。

## 下一步

接上 Jamendo(需要 Client ID),把範例資料換成真實音樂,並加入自訂分類管理。
