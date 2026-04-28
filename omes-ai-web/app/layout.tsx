import "./globals.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "OMES Industrial AI Chat",
  description: "Industrial multi-agent chat UI powered by OMES AI"
};

export default function RootLayout({
  children
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
