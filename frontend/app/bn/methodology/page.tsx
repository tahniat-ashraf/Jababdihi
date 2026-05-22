export default function MethodologyPage() {
  return (
    <article className="mx-auto w-full max-w-2xl py-6">
      <p className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
        পদ্ধতি ও আইনি নোট
      </p>
      <h1 className="mt-2 font-serif text-[2rem] font-medium leading-tight tracking-[-0.025em] text-foreground">
        জবাবদিহি কীভাবে প্রকাশিত প্রতিবেদন ব্যবহার করে
      </h1>

      <section className="mt-6 space-y-3 font-serif text-[1rem] leading-relaxed text-foreground-soft">
        <p>
          জবাবদিহি একটি উৎসনির্ভর জনস্বার্থ ফিড। এটি তালিকাভুক্ত সংবাদমাধ্যম
          ও সরকারি উৎসের প্রকাশিত প্রতিবেদন একত্র করে, সরাসরি উৎস লিংক সংরক্ষণ
          করে, এবং পর্যালোচনাধীন ঘটনা প্রকাশিত ঘটনা থেকে আলাদা রাখে।
        </p>
        <p>
          ঘটনাগুলোর ভাষা নিরপেক্ষ রাখা হয়: “প্রতিবেদন অনুযায়ী”, “অভিযোগ”, এবং
          “উৎস জানিয়েছে” ধরনের শব্দ ব্যবহার করা হয়। এই ব্যবস্থা আইনি সত্য,
          দোষ, বা দায় নির্ধারণ করে না।
        </p>
      </section>

      <section className="mt-8">
        <h2 className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
          আস্থা
        </h2>
        <p className="mt-2 font-serif text-[1rem] leading-relaxed text-foreground-soft">
          আস্থা স্কোর উৎস-সমর্থনের শক্তি বোঝায়। এটি উৎসের সংখ্যা, স্বাধীন
          প্রকাশকের সংখ্যা, এবং মেটাডেটার সামঞ্জস্যের ভিত্তিতে হিসাব করা হয়।
          এটি কোনো আইনি সিদ্ধান্ত নয়।
        </p>
      </section>

      <section className="mt-8" id="legal">
        <h2 className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
          আইনি সতর্কীকরণ
        </h2>
        <p className="mt-2 border-l-2 border-foreground bg-paper px-4 py-3 font-serif text-[0.95rem] italic leading-relaxed text-foreground-soft">
          এই প্ল্যাটফর্ম তালিকাভুক্ত উৎসের প্রকাশিত ঘটনা ও অভিযোগ একত্র করে।
          আস্থা স্কোর উৎস-সমর্থনের শক্তি বোঝায়; এটি আইনি প্রমাণ, দোষ, বা
          আদালতের সিদ্ধান্ত নয়।
        </p>
      </section>
    </article>
  );
}
