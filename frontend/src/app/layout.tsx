import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "esg-t3 — ESG 공시지원 시스템",
  description: "운영·거버넌스가 견고한 ESG 공시 데이터 플랫폼",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body className="font-sans antialiased">{children}</body>
    </html>
  );
}
