"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

type Props = {
  incidentId: string;
  currentStatus: string;
};

type Action = "publish" | "reject" | "archive" | "reprocess";

const actionConfig: Record<
  Action,
  { label: string; className: string; confirm: string }
> = {
  publish: {
    label: "Publish",
    className:
      "rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50",
    confirm: "Publish this incident to the public feed?"
  },
  reject: {
    label: "Reject",
    className:
      "rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50",
    confirm: "Reject this incident?"
  },
  archive: {
    label: "Archive",
    className:
      "rounded-md border bg-background px-4 py-2 text-sm font-medium hover:bg-muted disabled:opacity-50",
    confirm: "Archive this incident?"
  },
  reprocess: {
    label: "Reprocess",
    className:
      "rounded-md border bg-background px-4 py-2 text-sm font-medium hover:bg-muted disabled:opacity-50",
    confirm: "Send this incident back for AI reprocessing?"
  }
};

export function AdminActions({ incidentId, currentStatus }: Props) {
  const router = useRouter();
  const [pending, setPending] = useState<Action | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function runAction(action: Action) {
    const { confirm: message } = actionConfig[action];
    if (!window.confirm(message)) return;
    setPending(action);
    setError(null);
    try {
      const response = await fetch(
        `/api/admin/incidents/${incidentId}/${action}`,
        { method: "POST", headers: { "content-type": "application/json" } }
      );
      if (!response.ok) {
        const data = (await response.json().catch(() => null)) as {
          message?: string;
        } | null;
        setError(data?.message ?? `Action failed (${response.status})`);
        return;
      }
      router.push("/admin");
      router.refresh();
    } catch {
      setError("Network error — please try again.");
    } finally {
      setPending(null);
    }
  }

  const disabled = pending !== null;

  return (
    <div className="mt-6 border-t pt-6">
      <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
        Actions
      </p>
      {error ? (
        <p className="mb-3 rounded-md border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      ) : null}
      <p className="mb-4 text-xs text-muted-foreground">
        Status: <span className="font-medium">{currentStatus}</span>
      </p>
      <div className="flex flex-wrap gap-2">
        {(Object.keys(actionConfig) as Action[]).map((action) => (
          <button
            key={action}
            className={actionConfig[action].className}
            disabled={disabled}
            onClick={() => runAction(action)}
          >
            {pending === action ? "…" : actionConfig[action].label}
          </button>
        ))}
      </div>
    </div>
  );
}
