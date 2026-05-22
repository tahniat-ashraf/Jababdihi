import { notFound } from "next/navigation";
import type { Metadata } from "next";
import { IncidentDetail } from "@/components/incident-detail";
import { fetchIncidentDetail } from "@/lib/server-api";

type Props = { params: Promise<{ id: string }> };

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params;
  const incident = await fetchIncidentDetail(id, "bn").catch(() => null);
  if (!incident) return {};
  return { title: `${incident.title} — Jababdihi` };
}

export default async function Page({ params }: Props) {
  const { id } = await params;
  const incident = await fetchIncidentDetail(id, "bn");
  if (!incident) notFound();
  return <IncidentDetail incident={incident} language="bn" />;
}
